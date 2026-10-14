package com.picka_chew.dangcharge.domain.station.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.picka_chew.dangcharge.domain.station.entity.Station;
import com.picka_chew.dangcharge.domain.vehicle.entity.VehicleModel;

public interface StationRepository extends JpaRepository<Station, Long> {

	@Query("select distinct s from Station s left join fetch s.supportedModels")
	List<Station> findAllWithSupportedModels();

	@Query("select s from Station s where :model member of s.supportedModels")
	Page<Station> findAllSupporting(@Param("model") VehicleModel model, Pageable pageable);

	@Query("select s from Station s where s.metro = :metro and s.city = :city and :model member of s.supportedModels")
	Page<Station> findAllSupportingIn(@Param("model") VehicleModel model, @Param("metro") String metro,
			@Param("city") String city, Pageable pageable);

}
