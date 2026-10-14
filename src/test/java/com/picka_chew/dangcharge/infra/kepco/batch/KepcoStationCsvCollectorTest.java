package com.picka_chew.dangcharge.infra.kepco.batch;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.sun.net.httpserver.HttpServer;

import tools.jackson.databind.json.JsonMapper;

class KepcoStationCsvCollectorTest {

	@TempDir
	Path directory;

	private HttpServer server;
	private URI endpoint;

	@BeforeEach
	void startServer() throws IOException {
		server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
		endpoint = URI.create("http://127.0.0.1:" + server.getAddress().getPort() + "/EVcharge.do");
	}

	@AfterEach
	void stopServer() {
		server.stop(0);
	}

	@Test
	void writesCompleteCsvIncludingQuotedCarTypes() throws Exception {
		server.createContext("/EVcharge.do", exchange -> {
			if (exchange.getRequestURI().getQuery().contains("metroCd=29")) {
				respond(exchange, 404, "{\"errCd\":\"404\",\"errMsg\":\"NotFound\"}");
				return;
			}
			respond(exchange, 200, """
					{"data":[{"metro":"서울특별시","city":"강남구","stnPlace":"테스트 충전소",
					"stnAddr":"서울특별시 강남구","rapidCnt":1,"slowCnt":2,
					"carType":"레이EV,테슬라"}]}
					""");
		});
		server.start();
		Path output = directory.resolve("stations.csv");

		int count = collector(output, List.of("11", "29")).collect();

		assertThat(count).isEqualTo(1);
		assertThat(Files.readAllBytes(output)).startsWith((byte) 0xEF, (byte) 0xBB, (byte) 0xBF);
		try (var reader = Files.newBufferedReader(output, StandardCharsets.UTF_8)) {
			reader.read(); // UTF-8 BOM
			try (CSVParser csv = CSVFormat.DEFAULT.builder().setHeader().setSkipHeaderRecord(true).get().parse(reader)) {
				assertThat(csv.getHeaderNames()).containsExactly(
						"metro", "city", "stnPlace", "stnAddr", "rapidCnt", "slowCnt", "carType");
				var records = csv.getRecords();
				assertThat(records).hasSize(1);
				assertThat(records.get(0).get("carType")).isEqualTo("레이EV,테슬라");
			}
		}
	}

	@Test
	void failedRegionKeepsPreviousCsv() throws Exception {
		server.createContext("/EVcharge.do", exchange -> {
			if (exchange.getRequestURI().getQuery().contains("metroCd=31")) {
				respond(exchange, 400, "invalid region");
				return;
			}
			respond(exchange, 200, """
					{"data":[{"metro":"서울특별시","city":"강남구","stnPlace":"테스트 충전소",
					"stnAddr":"서울특별시 강남구","rapidCnt":1,"slowCnt":2,"carType":"레이EV"}]}
					""");
		});
		server.start();
		Path output = directory.resolve("stations.csv");
		Files.writeString(output, "previous snapshot");

		assertThatThrownBy(() -> collector(output, List.of("11", "31")).collect())
				.isInstanceOf(IOException.class)
				.hasMessageContaining("지역 31");
		assertThat(Files.readString(output)).isEqualTo("previous snapshot");
	}

	@Test
	void emptyRegionKeepsPreviousCsv() throws Exception {
		server.createContext("/EVcharge.do", exchange -> respond(exchange, 200, "{\"data\":[]}"));
		server.start();
		Path output = directory.resolve("stations.csv");
		Files.writeString(output, "previous snapshot");

		assertThatThrownBy(() -> collector(output, List.of("11")).collect())
				.isInstanceOf(IOException.class)
				.hasMessageContaining("지역 11");
		assertThat(Files.readString(output)).isEqualTo("previous snapshot");
	}

	@Test
	void writesThroughSymlinkedOutputDirectory() throws Exception {
		server.createContext("/EVcharge.do", exchange -> respond(exchange, 200, """
				{"data":[{"metro":"서울특별시","city":"강남구","stnPlace":"테스트 충전소",
				"stnAddr":"서울특별시 강남구","rapidCnt":1,"slowCnt":2,"carType":"레이EV"}]}
				"""));
		server.start();
		Path realDirectory = Files.createDirectory(directory.resolve("real"));
		Path link = Files.createSymbolicLink(directory.resolve("link"), realDirectory);

		int count = collector(link.resolve("stations.csv"), List.of("11")).collect();

		assertThat(count).isEqualTo(1);
		assertThat(Files.exists(realDirectory.resolve("stations.csv"))).isTrue();
	}

	private KepcoStationCsvCollector collector(Path output, List<String> codes) {
		return new KepcoStationCsvCollector(HttpClient.newHttpClient(), JsonMapper.builder().build(),
				endpoint, "secret", output, codes);
	}

	private static void respond(com.sun.net.httpserver.HttpExchange exchange, int status, String body)
			throws IOException {
		byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
		exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
		exchange.sendResponseHeaders(status, bytes.length);
		try (var output = exchange.getResponseBody()) {
			output.write(bytes);
		}
	}

}
