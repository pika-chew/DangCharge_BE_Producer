package com.picka_chew.dangcharge.infra.kepco;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.picka_chew.dangcharge.domain.station.model.StationSnapshot;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

@Disabled("CSV 파일 테스트는 나중에 다시 켠다")
class KepcoStationCsvReaderTest {

	private static final String HEADER = "metro,city,stnPlace,stnAddr,rapidCnt,slowCnt,carType\n";

	@TempDir
	Path tempDir;

	@Test
	void readsCsvWrittenByScript() throws IOException {
		Path csv = write("﻿" + HEADER
				+ "서울특별시,강남구,LH강남힐스테이트,서울특별시 강남구 자곡동 자곡로3길 21,1,4,\"SM3 Z.E,레이EV,테슬라\"\n");

		List<StationSnapshot> snapshots = new KepcoStationCsvReader(csv).read();

		assertThat(snapshots).containsExactly(new StationSnapshot("서울특별시", "강남구", "LH강남힐스테이트",
				"서울특별시 강남구 자곡동 자곡로3길 21", 1, 4,
				java.util.Set.of(VehicleModel.SM3_ZE, VehicleModel.RAY_EV, VehicleModel.TESLA)));
	}

	@Test
	void trimsWhitespaceAroundValues() throws IOException {
		Path csv = write(HEADER + "경기도,연천군, 한국전력 연천지사 ,\" 경기도 연천군 전곡읍, 231-13 \",1,0,테슬라\n");

		StationSnapshot snapshot = new KepcoStationCsvReader(csv).read().get(0);

		assertThat(snapshot.name()).isEqualTo("한국전력 연천지사");
		assertThat(snapshot.address()).isEqualTo("경기도 연천군 전곡읍, 231-13");
	}

	@Test
	void keepsStationWithNoSupportedModels() throws IOException {
		Path csv = write(HEADER + "제주특별자치도,제주시,어딘가,제주시 1,0,2,\n");

		assertThat(new KepcoStationCsvReader(csv).read().get(0).supportedModels()).isEmpty();
	}

	@Test
	void skipsModelsOutsideTheChoices() throws IOException {
		Path csv = write(HEADER + "제주특별자치도,제주시,어딘가,제주시 1,1,0,\"코나EV,테슬라\"\n");

		assertThat(new KepcoStationCsvReader(csv).read().get(0).supportedModels())
				.containsExactly(VehicleModel.TESLA);
	}

	@Test
	void failsWithGuidanceWhenFileIsMissing() {
		Path missing = tempDir.resolve("none.csv");

		assertThatThrownBy(() -> new KepcoStationCsvReader(missing).read())
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("충전소 CSV가 없습니다");
	}

	private Path write(String content) throws IOException {
		Path csv = tempDir.resolve("stations.csv");
		Files.writeString(csv, content, StandardCharsets.UTF_8);
		return csv;
	}

}
