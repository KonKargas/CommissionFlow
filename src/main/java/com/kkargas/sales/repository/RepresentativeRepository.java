package com.kkargas.sales.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.kkargas.sales.model.Representative;

@Repository
public interface RepresentativeRepository extends JpaRepository<Representative, Long> {

}