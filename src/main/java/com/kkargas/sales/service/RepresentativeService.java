package com.kkargas.sales.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kkargas.sales.model.Representative;
import com.kkargas.sales.repository.RepresentativeRepository;

import jakarta.transaction.Transactional;

@Service
public class RepresentativeService {

    private final RepresentativeRepository representativeRepository;


    public RepresentativeService(
            RepresentativeRepository representativeRepository) {

        this.representativeRepository = representativeRepository;
    }


    public List<Representative> getAllRepresentatives() {

        return representativeRepository.findAll();
    }


    public Representative getRepresentativeById(Long id) {

        return representativeRepository
                .findById(id)
                .orElse(null);
    }


    @Transactional
    public Representative saveRepresentative(
            Representative representative) {

        // CREATE
        if (representative.getRepId() == null) {

            return representativeRepository.save(
                    representative
            );
        }


        // UPDATE
        Representative existing =
                representativeRepository
                        .findById(
                                representative.getRepId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Representative does not exist."
                                )
                        );


        existing.setFullName(
                representative.getFullName()
        );

        existing.setVatNumber(
                representative.getVatNumber()
        );


        return representativeRepository.save(
                existing
        );
    }


    @Transactional
    public void deleteRepresentative(Long id) {

        Representative representative =
                representativeRepository
                        .findById(id)
                        .orElse(null);

        if (representative == null) {
            return;
        }

        representativeRepository.delete(
                representative
        );
    }


    public long countRepresentatives() {

        return representativeRepository.count();
    }
}