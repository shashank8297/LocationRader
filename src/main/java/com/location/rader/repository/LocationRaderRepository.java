package com.location.rader.repository;

import org.springframework.stereotype.Repository;

import com.location.rader.model.Coordinates;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

@Repository
public interface LocationRaderRepository extends JpaRepository<Coordinates, Long> {

	List<Coordinates> findByUserIdOrderByTimestampDesc(Long userId);

}
