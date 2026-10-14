# DangCharge Producer

## 충전소 CSV 수집 배치

저장소 루트의 `.env`에 `KEPCO_API_KEY=...`를 설정하거나 환경 변수로 전달한 뒤 Spring Batch 작업을 실행합니다.

```bash
./gradlew stationCsvBatch
```

전국 지역별 한전 설치현황을 받아 `data/ev_charge_stations.csv`에 저장합니다. 모든 지역 요청과 검증이 끝나야 기존 CSV를 교체합니다. 세종 지역은 현재 한전 API에서 `404 NotFound`를 반환해 0건으로 처리합니다. `.env`와 생성된 `data/` 디렉터리는 Git에서 제외됩니다. 배치 작업은 한 번 실행 후 종료하며 DB 연결이나 배치 메타데이터 테이블을 사용하지 않습니다.

다른 경로에 저장해 점검하려면 `STATION_BATCH_CSV_PATH=/tmp/ev_charge_stations.csv ./gradlew stationCsvBatch`를 실행합니다. 정기 실행은 이 명령을 스케줄러에서 호출하면 됩니다.

```bash
./gradlew test
```
