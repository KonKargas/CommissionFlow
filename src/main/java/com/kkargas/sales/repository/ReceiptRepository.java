package com.kkargas.sales.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.kkargas.sales.model.Receipt;

@Repository
public interface ReceiptRepository extends JpaRepository<Receipt, Long> {

	List<Receipt> findByRepresentativeRepId(Long repId);
	
	Optional<Receipt> findByReceiptCode(Long receiptCode);

    @Query("""
           SELECT COALESCE(MAX(r.receiptCode), 0)
           FROM Receipt r
           """)
    Long findMaxReceiptCode();
    
    List<Receipt> findByArtisanshipArtisanshipId(
            Long artisanshipId
    );
	
}