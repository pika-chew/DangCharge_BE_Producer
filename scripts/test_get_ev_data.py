"""Checks for safe CSV replacement and transient API failures."""

import csv
import io
import json
import tempfile
import unittest
from contextlib import redirect_stderr, redirect_stdout
from pathlib import Path
from unittest.mock import patch
from urllib.error import HTTPError

import get_ev_data


ROW = {
    "metro": "서울특별시", "city": "강남구", "stnPlace": "테스트 충전소",
    "stnAddr": "서울특별시 강남구", "rapidCnt": 1, "slowCnt": 2,
    "carType": "레이EV,테슬라",
}


class FakeResponse:
    def __enter__(self):
        return self

    def __exit__(self, *_args):
        return False

    def read(self):
        return json.dumps({"data": [ROW]}).encode("utf-8")


class GetEvDataTest(unittest.TestCase):
    def test_sejong_not_found_means_no_data(self):
        def opener(_request, timeout):
            raise HTTPError("redacted", 404, "NotFound", {}, io.BytesIO())

        self.assertEqual([], get_ev_data.fetch_region("29", "secret", opener=opener))

    def test_retries_transient_http_error(self):
        calls = []
        sleeps = []

        def opener(_request, timeout):
            calls.append(timeout)
            if len(calls) == 1:
                raise HTTPError("redacted", 503, "busy", {}, io.BytesIO())
            return FakeResponse()

        rows = get_ev_data.fetch_region("11", "secret", opener=opener, sleeper=sleeps.append)

        self.assertEqual(2, len(calls))
        self.assertEqual([1], sleeps)
        self.assertEqual(1, rows[0]["rapidCnt"])

    def test_failure_keeps_existing_csv(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / "stations.csv"
            output.write_bytes(b"original csv")
            with patch.dict("os.environ", {"KEPCO_API_KEY": "secret"}), \
                    patch.object(get_ev_data, "METRO_CODES", ("11", "31")), \
                    patch.object(get_ev_data, "fetch_region", side_effect=[
                        [get_ev_data.validate_row(ROW, "11")], get_ev_data.FetchError("failed")
                    ]):
                with redirect_stdout(io.StringIO()), redirect_stderr(io.StringIO()):
                    result = get_ev_data.main(["--output", str(output)])

            self.assertEqual(1, result)
            self.assertEqual(b"original csv", output.read_bytes())

    def test_success_writes_utf8_bom_and_expected_columns(self):
        with tempfile.TemporaryDirectory() as directory:
            output = Path(directory) / "stations.csv"
            with patch.dict("os.environ", {"KEPCO_API_KEY": "secret"}), \
                    patch.object(get_ev_data, "METRO_CODES", ("11",)), \
                    patch.object(get_ev_data, "fetch_region", return_value=[
                        get_ev_data.validate_row(ROW, "11")
                    ]):
                with redirect_stdout(io.StringIO()):
                    result = get_ev_data.main(["--output", str(output)])

            self.assertEqual(0, result)
            self.assertTrue(output.read_bytes().startswith(b"\xef\xbb\xbf"))
            with output.open(encoding="utf-8-sig", newline="") as file:
                rows = list(csv.DictReader(file))
            self.assertEqual(list(get_ev_data.COLUMNS), list(rows[0]))
            self.assertEqual("테스트 충전소", rows[0]["stnPlace"])


if __name__ == "__main__":
    unittest.main()
