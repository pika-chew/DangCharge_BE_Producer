package com.picka_chew.dangcharge.domain.station.repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

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

	@Query("select distinct s from Station s left join fetch s.supportedModels where s.id = :id")
	Optional<Station> findWithSupportedModelsById(@Param("id") Long id);

	@Query("""
			select s from Station s
			where (:keyword is null or s.name like concat('%', :keyword, '%') or s.address like concat('%', :keyword, '%'))
			and (:metro is null or s.metro = :metro)
			and (:city is null or s.city = :city)
			order by s.name, s.id
			""")
	List<Station> search(@Param("keyword") String keyword, @Param("metro") String metro, @Param("city") String city,
			Pageable pageable);

	/** 좌표가 있는 충전소 중 위경도 범위 안에 있는 것. 정확한 거리 계산은 호출하는 쪽에서 한다 */
	@Query("""
			select s from Station s
			where s.latitude between :minLat and :maxLat
			and s.longitude between :minLng and :maxLng
			""")
	List<Station> findAllInBox(@Param("minLat") BigDecimal minLat, @Param("maxLat") BigDecimal maxLat,
			@Param("minLng") BigDecimal minLng, @Param("maxLng") BigDecimal maxLng);

}
