# DangCharge Producer

## 충전소 CSV 수집 배치

저장소 루트의 `.env`에 `KEPCO_API_KEY=...`를 설정하거나 환경 변수로 전달한 뒤 실행합니다.

```bash
python3 scripts/get_ev_data.py
```

전국 지역별 한전 설치현황을 받아 `data/ev_charge_stations.csv`에 저장합니다. Python 3 표준 라이브러리만 사용하며, 모든 지역 요청이 끝나야 기존 CSV를 교체합니다. 세종 지역은 현재 한전 API에서 `404 NotFound`를 반환해 0건으로 처리합니다. `.env`와 생성된 `data/` 디렉터리는 Git에서 제외됩니다.

다른 경로에 저장해 점검하려면 `python3 scripts/get_ev_data.py --output /tmp/ev_charge_stations.csv`를 실행합니다. 배치는 실행할 때마다 한 번 수집하며, 정기 실행은 이 명령을 스케줄러에서 호출하면 됩니다.

```bash
python3 -m unittest discover -s scripts -p 'test_*.py'
```
