"""Fetch KEPCO EV charging station installation data into a local CSV.

Run from any directory with ``python3 scripts/get_ev_data.py``. Set
``KEPCO_API_KEY`` in the environment or in the repository root's ``.env``.
The output is replaced only after every regional request has succeeded.
"""

import argparse
import csv
import json
import os
import sys
import tempfile
import time
from pathlib import Path
from urllib import error, parse, request


ROOT = Path(__file__).resolve().parent.parent
API_URL = "https://bigdata.kepco.co.kr/openapi/v1/EVcharge.do"
METRO_CODES = (
    "11", "31", "23", "32", "34", "29", "25", "33", "37",
    "38", "35", "22", "26", "21", "24", "36", "39",
)
COLUMNS = ("metro", "city", "stnPlace", "stnAddr", "rapidCnt", "slowCnt", "carType")
RETRYABLE_HTTP_CODES = {429, 500, 502, 503, 504}
MAX_ATTEMPTS = 3
# KEPCO currently responds with 404 NotFound for Sejong rather than an empty list.
NO_DATA_HTTP_CODES = {"29": 404}


class FetchError(Exception):
    """The source response is unavailable or cannot be safely written."""


def api_key_from_env():
    key = os.environ.get("KEPCO_API_KEY", "").strip()
    if key:
        return key

    env_file = ROOT / ".env"
    if env_file.is_file():
        for line in env_file.read_text(encoding="utf-8").splitlines():
            name, separator, value = line.strip().partition("=")
            if separator and name == "KEPCO_API_KEY":
                return value.strip().strip('"').strip("'")
    return ""


def fetch_region(code, api_key, opener=request.urlopen, sleeper=time.sleep):
    url = API_URL + "?" + parse.urlencode(
        {"metroCd": code, "apiKey": api_key, "returnType": "json"}
    )
    http_request = request.Request(url, headers={"User-Agent": "DangCharge-Station-Batch/1.0"})

    for attempt in range(1, MAX_ATTEMPTS + 1):
        try:
            with opener(http_request, timeout=20) as response:
                payload = json.loads(response.read().decode("utf-8-sig"))
            if not isinstance(payload, dict) or not isinstance(payload.get("data"), list):
                raise FetchError(f"지역 {code}: 응답에 data 목록이 없습니다")
            return [validate_row(row, code) for row in payload["data"]]
        except error.HTTPError as exc:
            status = exc.code
            exc.close()
            if NO_DATA_HTTP_CODES.get(code) == status:
                return []
            if status not in RETRYABLE_HTTP_CODES or attempt == MAX_ATTEMPTS:
                raise FetchError(f"지역 {code}: HTTP {status} 응답") from None
        except (error.URLError, TimeoutError, OSError) as exc:
            if attempt == MAX_ATTEMPTS:
                raise FetchError(f"지역 {code}: 연결 실패 ({type(exc).__name__})") from None
        except (UnicodeError, json.JSONDecodeError):
            raise FetchError(f"지역 {code}: JSON 응답을 읽을 수 없습니다") from None
        sleeper(attempt)

    raise AssertionError("unreachable")


def validate_row(row, code):
    if not isinstance(row, dict) or any(column not in row for column in COLUMNS):
        raise FetchError(f"지역 {code}: 필수 CSV 컬럼이 누락되었습니다")
    result = {column: "" if row[column] is None else str(row[column])
              for column in COLUMNS}
    if any(not result[column].strip() for column in ("metro", "stnPlace", "stnAddr")):
        raise FetchError(f"지역 {code}: 충전소 위치 정보가 비어 있습니다")
    for column in ("rapidCnt", "slowCnt"):
        try:
            count = int(result[column])
        except ValueError:
            raise FetchError(f"지역 {code}: {column} 값이 숫자가 아닙니다") from None
        if count < 0:
            raise FetchError(f"지역 {code}: {column} 값이 음수입니다")
        result[column] = count
    return result


def write_csv(rows, output):
    output.parent.mkdir(parents=True, exist_ok=True)
    temporary = None
    try:
        with tempfile.NamedTemporaryFile(
            mode="w", encoding="utf-8-sig", newline="", dir=output.parent,
            prefix=f".{output.name}.", suffix=".tmp", delete=False
        ) as file:
            temporary = Path(file.name)
            writer = csv.DictWriter(file, fieldnames=COLUMNS)
            writer.writeheader()
            writer.writerows(rows)
        os.replace(temporary, output)
    finally:
        if temporary is not None:
            temporary.unlink(missing_ok=True)


def main(argv=None):
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--output", type=Path, default=ROOT / "data" / "ev_charge_stations.csv",
        help="CSV 저장 경로 (기본값: 저장소의 data/ev_charge_stations.csv)",
    )
    args = parser.parse_args(argv)
    api_key = api_key_from_env()
    if not api_key:
        parser.error("KEPCO_API_KEY를 환경 변수 또는 저장소 루트의 .env에 설정하세요")

    try:
        rows = []
        for code in METRO_CODES:
            region_rows = fetch_region(code, api_key)
            rows.extend(region_rows)
            print(f"지역 {code}: {len(region_rows)}건")
        if not rows:
            raise FetchError("수집 결과가 비어 있어 CSV를 갱신하지 않았습니다")
        write_csv(rows, args.output)
    except (FetchError, OSError) as exc:
        print(f"충전소 수집 실패: {exc}", file=sys.stderr)
        return 1

    print(f"수집 완료: {len(rows)}건 -> {args.output}")
    return 0


if __name__ == "__main__":
    sys.exit(main())
