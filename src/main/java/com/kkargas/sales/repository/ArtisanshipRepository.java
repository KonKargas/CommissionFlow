package com.kkargas.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkargas.sales.model.Artisanship;

@Repository
public interface ArtisanshipRepository extends JpaRepository<Artisanship, Long> {

}