package com.kkargas.sales.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.kkargas.sales.model.Artisanship;
import com.kkargas.sales.model.Receipt;
import com.kkargas.sales.repository.ArtisanshipRepository;
import com.kkargas.sales.repository.ReceiptRepository;

import jakarta.transaction.Transactional;

@Service
public class ArtisanshipService {

	private final ArtisanshipRepository artisanshipRepository;
	private final ReceiptRepository receiptRepository;


	public ArtisanshipService(
	        ArtisanshipRepository artisanshipRepository,
	        ReceiptRepository receiptRepository) {

	    this.artisanshipRepository =
	            artisanshipRepository;

	    this.receiptRepository =
	            receiptRepository;
	}


    public List<Artisanship> getAllArtisanships() {

        return artisanshipRepository.findAll();
    }


    public Artisanship getArtisanshipById(Long id) {

        return artisanshipRepository
                .findById(id)
                .orElse(null);
    }


    public Artisanship saveArtisanship(
            Artisanship artisanship) {

        return artisanshipRepository.save(
                artisanship
        );
    }


    @Transactional
    public void deleteArtisanship(
            Long id) {

        Artisanship artisanship =
                artisanshipRepository
                        .findById(id)
                        .orElse(null);


        if (artisanship == null) {
            return;
        }


        List<Receipt> receipts =
                receiptRepository
                        .findByArtisanshipArtisanshipId(
                                id
                        );


        if (!receipts.isEmpty()) {

            receiptRepository.deleteAll(
                    receipts
            );
        }


        artisanshipRepository.delete(
                artisanship
        );
    }


    public long countArtisanships() {

        return artisanshipRepository.count();
    }
}