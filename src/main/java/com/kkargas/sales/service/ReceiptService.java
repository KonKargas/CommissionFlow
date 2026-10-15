package com.kkargas.sales.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.stereotype.Service;

import com.kkargas.sales.enums.ReceiptType;
import com.kkargas.sales.model.Receipt;
import com.kkargas.sales.repository.ReceiptRepository;

@Service
public class ReceiptService {

    private final ReceiptRepository receiptRepository;


    public ReceiptService(
            ReceiptRepository receiptRepository) {

        this.receiptRepository = receiptRepository;
    }


    public List<Receipt> getAllReceipts() {

        return receiptRepository.findAll();
    }


    public Receipt getReceiptById(Long id) {

        return receiptRepository
                .findById(id)
                .orElse(null);
    }


    public Receipt saveReceipt(Receipt receipt) {

        return receiptRepository.save(receipt);
    }


    public void deleteReceipt(Long id) {

        receiptRepository.deleteById(id);
    }


    public long countReceipts() {

        return receiptRepository.count();
    }


    // =========================================
    // REPRESENTATIVE SALES
    // =========================================

    public List<Receipt> getReceiptsByRepresentative(Long repId) {

        return receiptRepository
                .findByRepresentativeRepId(repId);
    }


    public BigDecimal getTotalSalesByRepresentative(Long repId) {

        return getReceiptsByRepresentative(repId)
                .stream()
                .map(Receipt::getSalesPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public int getTotalItemsByRepresentative(Long repId) {

        return getReceiptsByRepresentative(repId)
                .stream()
                .map(Receipt::getSalesAmount)
                .filter(amount -> amount != null)
                .mapToInt(Integer::intValue)
                .sum();
    }


    public BigDecimal getSalesByCategory(
            Long repId,
            ReceiptType receiptType) {

        return getReceiptsByRepresentative(repId)
                .stream()
                .filter(receipt ->
                        receipt.getReceiptType() == receiptType)
                .map(Receipt::getSalesPrice)
                .filter(price -> price != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }


    public BigDecimal calculateCommission(Long repId) {

        BigDecimal totalSales =
                getTotalSalesByRepresentative(repId);

        BigDecimal commission;


        if (totalSales.compareTo(
                BigDecimal.valueOf(6000)) <= 0) {

            commission = BigDecimal.ZERO;
        }

        else if (totalSales.compareTo(
                BigDecimal.valueOf(10000)) <= 0) {

            commission = totalSales
                    .subtract(BigDecimal.valueOf(6000))
                    .multiply(BigDecimal.valueOf(0.10));
        }

        else if (totalSales.compareTo(
                BigDecimal.valueOf(40000)) <= 0) {

            BigDecimal firstPart =
                    BigDecimal.valueOf(10000)
                            .multiply(BigDecimal.valueOf(0.10));

            BigDecimal secondPart =
                    totalSales
                            .subtract(BigDecimal.valueOf(10000))
                            .multiply(BigDecimal.valueOf(0.15));

            commission =
                    firstPart.add(secondPart);
        }

        else {

            BigDecimal firstPart =
                    BigDecimal.valueOf(10000)
                            .multiply(BigDecimal.valueOf(0.10));

            BigDecimal secondPart =
                    BigDecimal.valueOf(30000)
                            .multiply(BigDecimal.valueOf(0.15));

            BigDecimal thirdPart =
                    totalSales
                            .subtract(BigDecimal.valueOf(40000))
                            .multiply(BigDecimal.valueOf(0.20));

            commission =
                    firstPart
                            .add(secondPart)
                            .add(thirdPart);
        }


        return commission.setScale(
                2,
                RoundingMode.HALF_UP
        );
    }
    
    public Receipt getReceiptByCode(Long receiptCode) {

        return receiptRepository
                .findByReceiptCode(receiptCode)
                .orElse(null);
    }


    public Long getMaxReceiptCode() {

        return receiptRepository
                .findMaxReceiptCode();
    }
    
    public BigDecimal getTotalSales() {

        return getAllReceipts()
                .stream()
                .map(Receipt::getSalesPrice)
                .filter(price -> price != null)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }


    public BigDecimal getTotalSalesByCategory(
            ReceiptType receiptType) {

        return getAllReceipts()
                .stream()
                .filter(receipt ->
                        receipt.getReceiptType()
                                == receiptType
                )
                .map(Receipt::getSalesPrice)
                .filter(price ->
                        price != null
                )
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }
}