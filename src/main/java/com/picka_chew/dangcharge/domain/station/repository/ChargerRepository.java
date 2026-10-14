package com.picka_chew.dangcharge.domain.station.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.picka_chew.dangcharge.domain.station.entity.Charger;

import jakarta.persistence.LockModeType;

public interface ChargerRepository extends JpaRepository<Charger, Long> {

	List<Charger> findAllByStationIdOrderById(Long stationId);

	List<Charger> findAllByStationIdIn(Collection<Long> stationIds);

	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("select c from Charger c where c.id = :id")
	Optional<Charger> findByIdForUpdate(@Param("id") Long id);

}
