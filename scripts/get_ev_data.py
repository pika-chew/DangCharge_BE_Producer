"""한전 전기차 충전소 설치현황을 받아 data/ev_charge_stations.csv로 저장한다.

실행: KEPCO_API_KEY=... python scripts/get_ev_data.py
필요 패키지: pandas, requests
"""
import os
import sys
from pathlib import Path

import pandas as pd
import requests

# 1. API 기본 설정
url = "https://bigdata.kepco.co.kr/openapi/v1/EVcharge.do"
api_key = os.environ.get("KEPCO_API_KEY")
if not api_key:
  sys.exit("환경 변수 KEPCO_API_KEY가 필요합니다.")

# 전국 17개 광역자치단체 공통코드 리스트
metro_codes = [
    "11",  # 서울
    "31",  # 경기
    "23",  # 인천
    "32",  # 강원
    "34",  # 충남
    "29",  # 세종
    "25",  # 대전
    "33",  # 충북
    "37",  # 경북
    "38",  # 경남
    "35",  # 전북
    "22",  # 대구
    "26",  # 울산
    "21",  # 부산
    "24",  # 광주
    "36",  # 전남
    "39",  # 제주
]

all_data = []

# 2. 광역자치단체별 데이터 수집
for code in metro_codes:
  params = {"metroCd": code, "apiKey": api_key, "returnType": "json"}

  response = requests.get(url, params=params)

  # 응답 데이터 인코딩 명시 (한글 깨짐 방지)
  response.encoding = "utf-8"

  if response.status_code == 200:
    try:
      result = response.json()
      items = result.get("data", [])
      all_data.extend(items)
      print(f"코드 {code} 수집 완료: {len(items)}건")
    except ValueError:
      print(f"코드 {code} JSON 파싱 실패")
  else:
    print(f"코드 {code} 요청 실패: {response.status_code}")

# 3. DataFrame 변환 및 CSV 저장 (utf-8-sig 인코딩 적용)
if all_data:
  df = pd.DataFrame(all_data)

  # 생성된 데이터는 저장소 루트의 data/ 아래에 두며 git에 올리지 않는다.
  output_path = Path(__file__).resolve().parent.parent / "data" / "ev_charge_stations.csv"
  output_path.parent.mkdir(parents=True, exist_ok=True)

  # utf-8-sig: UTF-8 BOM을 추가하여 엑셀(Excel)에서 한글이 깨지는 문제 방지
  df.to_csv(output_path, index=False, encoding="utf-8-sig")
  print(f"\n총 {len(all_data)}건의 데이터 변환 완료: {output_path}")
else:
  print("\n수집된 데이터가 없습니다.")
