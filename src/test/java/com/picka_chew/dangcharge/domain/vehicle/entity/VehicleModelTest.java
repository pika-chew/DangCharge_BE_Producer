package com.picka_chew.dangcharge.domain.vehicle.entity;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class VehicleModelTest {

	@Test
	void findsModelByKepcoLabel() {
		assertThat(VehicleModel.fromLabel("SM3 Z.E")).contains(VehicleModel.SM3_ZE);
		assertThat(VehicleModel.fromLabel("테슬라")).contains(VehicleModel.TESLA);
	}

	@Test
	void ignoresSurroundingWhitespace() {
		assertThat(VehicleModel.fromLabel(" BMW i3 ")).contains(VehicleModel.BMW_I3);
	}

	@Test
	void returnsEmptyForUnknownOrMissingLabel() {
		assertThat(VehicleModel.fromLabel("코나EV")).isEmpty();
		assertThat(VehicleModel.fromLabel(null)).isEmpty();
	}

}
