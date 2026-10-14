package com.picka_chew.dangcharge.infra.kepco.batch;

import java.io.IOException;
import java.io.Writer;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import com.picka_chew.dangcharge.api.station.port.StationCsvCollector;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

/** Fetches each KEPCO region and atomically publishes one complete CSV snapshot. */
@Slf4j
@Component
@Profile("station-csv-batch")
public class KepcoStationCsvCollector implements StationCsvCollector {

	private static final URI API_URI = URI.create("https://bigdata.kepco.co.kr/openapi/v1/EVcharge.do");
	private static final List<String> METRO_CODES = List.of(
			"11", "31", "23", "32", "34", "29", "25", "33", "37",
			"38", "35", "22", "26", "21", "24", "36", "39");
	private static final String[] COLUMNS = {
			"metro", "city", "stnPlace", "stnAddr", "rapidCnt", "slowCnt", "carType"
	};
	private static final int MAX_ATTEMPTS = 3;

	private final HttpClient client;
	private final JsonMapper mapper;
	private final URI endpoint;
	private final String apiKey;
	private final Path output;
	private final List<String> metroCodes;

	@Autowired
	public KepcoStationCsvCollector(
			@Value("${KEPCO_API_KEY:}") String apiKey,
			@Value("${STATION_BATCH_CSV_PATH:data/ev_charge_stations.csv}") String outputPath) {
		this(HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(20)).build(),
				JsonMapper.builder().build(), API_URI, apiKey, Path.of(outputPath), METRO_CODES);
	}

	KepcoStationCsvCollector(HttpClient client, JsonMapper mapper, URI endpoint,
			String apiKey, Path output, List<String> metroCodes) {
		this.client = client;
		this.mapper = mapper;
		this.endpoint = endpoint;
		this.apiKey = apiKey;
		this.output = output;
		this.metroCodes = metroCodes;
	}

	@Override
	public int collect() throws IOException, InterruptedException {
		if (apiKey.isBlank()) {
			throw new IllegalStateException("KEPCO_API_KEY를 환경 변수 또는 .env에 설정하세요");
		}

		List<StationRow> rows = new ArrayList<>();
		for (String code : metroCodes) {
			List<StationRow> regionRows = fetchRegion(code);
			rows.addAll(regionRows);
			log.info("지역 {}: {}건", code, regionRows.size());
		}
		if (rows.isEmpty()) {
			throw new IOException("수집 결과가 비어 있어 기존 CSV를 보존합니다");
		}
		publish(rows);
		log.info("충전소 CSV 수집 완료: {}건 -> {}", rows.size(), output.toAbsolutePath());
		return rows.size();
	}

	private List<StationRow> fetchRegion(String code) throws IOException, InterruptedException {
		String query = "metroCd=" + code + "&apiKey=" + URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
				+ "&returnType=json";
		HttpRequest request = HttpRequest.newBuilder(URI.create(endpoint + "?" + query))
				.timeout(Duration.ofSeconds(20))
				.header("User-Agent", "DangCharge-Station-Batch/1.0")
				.GET()
				.build();

		for (int attempt = 1; attempt <= MAX_ATTEMPTS; attempt++) {
			HttpResponse<String> response;
			try {
				response = client.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
			}
			catch (IOException exception) {
				if (attempt == MAX_ATTEMPTS) {
					throw new IOException("지역 " + code + " 연결 실패", exception);
				}
				Thread.sleep(attempt * 1000L);
				continue;
			}

			int status = response.statusCode();
			if (status == 200) {
				List<StationRow> rows = parseRows(code, response.body());
				if (rows.isEmpty() && !code.equals("29")) {
					throw new IOException("지역 " + code + " 수집 결과가 비어 있습니다");
				}
				return rows;
			}
			// KEPCO currently uses 404 to represent no Sejong records.
			if (code.equals("29") && status == 404) {
				return List.of();
			}
			if ((status == 429 || status >= 500) && attempt < MAX_ATTEMPTS) {
				Thread.sleep(attempt * 1000L);
				continue;
			}
			throw new IOException("지역 " + code + " 요청 실패: HTTP " + status);
		}
		throw new IllegalStateException("재시도 횟수를 초과했습니다");
	}

	private List<StationRow> parseRows(String code, String body) throws IOException {
		JsonNode root;
		try {
			root = mapper.readTree(body);
		}
		catch (RuntimeException exception) {
			throw new IOException("지역 " + code + " JSON 파싱 실패", exception);
		}
		JsonNode data = root == null ? null : root.get("data");
		if (data == null || !data.isArray()) {
			throw new IOException("지역 " + code + " 응답에 data 목록이 없습니다");
		}

		List<StationRow> rows = new ArrayList<>();
		for (JsonNode item : data) {
			String metro = field(item, "metro", code);
			String city = field(item, "city", code);
			String place = field(item, "stnPlace", code);
			String address = field(item, "stnAddr", code);
			String rapid = field(item, "rapidCnt", code);
			String slow = field(item, "slowCnt", code);
			String carType = field(item, "carType", code);
			if (metro.isBlank() || place.isBlank() || address.isBlank()) {
				throw new IOException("지역 " + code + " 충전소 위치 정보가 비어 있습니다");
			}
			rows.add(new StationRow(metro, city, place, address,
					parseCount(rapid, "rapidCnt", code), parseCount(slow, "slowCnt", code), carType));
		}
		return rows;
	}

	private static String field(JsonNode item, String name, String code) throws IOException {
		JsonNode value = item == null ? null : item.get(name);
		if (value == null) {
			throw new IOException("지역 " + code + " 응답에 " + name + " 컬럼이 없습니다");
		}
		return value.isNull() ? "" : value.asText();
	}

	private static String parseCount(String value, String name, String code) throws IOException {
		try {
			int count = Integer.parseInt(value.trim());
			if (count >= 0) {
				return Integer.toString(count);
			}
		}
		catch (NumberFormatException ignored) {
			// The error below identifies the source field without logging its response body.
		}
		throw new IOException("지역 " + code + " 응답의 " + name + " 값이 유효하지 않습니다");
	}

	private void publish(List<StationRow> rows) throws IOException {
		Path target = output.toAbsolutePath();
		if (!Files.isDirectory(target.getParent())) {
			Files.createDirectories(target.getParent());
		}
		Path temporary = Files.createTempFile(target.getParent(), ".ev-charge-stations-", ".tmp");
		try {
			try (Writer writer = Files.newBufferedWriter(temporary, StandardCharsets.UTF_8)) {
				writer.write('\uFEFF');
				try (CSVPrinter csv = new CSVPrinter(writer, CSVFormat.DEFAULT.builder().setHeader(COLUMNS).get())) {
					for (StationRow row : rows) {
						csv.printRecord(row.metro(), row.city(), row.place(), row.address(),
								row.rapid(), row.slow(), row.carType());
					}
				}
			}
			Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
		}
		finally {
			Files.deleteIfExists(temporary);
		}
	}

	private record StationRow(String metro, String city, String place, String address,
			String rapid, String slow, String carType) {
	}

}
