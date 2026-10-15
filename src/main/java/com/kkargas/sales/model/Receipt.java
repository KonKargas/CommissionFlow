package com.kkargas.sales.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.kkargas.sales.enums.ReceiptType;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
public class Receipt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long receiptId;

    @Column(unique = true, nullable = false)
    private Long receiptCode;

    private LocalDate salesDate;

    private BigDecimal salesPrice;

    private Integer salesAmount;

    @Enumerated(EnumType.STRING)
    private ReceiptType receiptType;

    @ManyToOne
    @JoinColumn(name = "representative_id")
    private Representative representative;

    @ManyToOne
    @JoinColumn(name = "artisanship_id")
    private Artisanship artisanship;

    public Receipt() {
    }

	public Long getReceiptId() {
		return receiptId;
	}

	public void setReceiptId(Long receiptId) {
		this.receiptId = receiptId;
	}

	public Long getReceiptCode() {
		return receiptCode;
	}

	public void setReceiptCode(Long receiptCode) {
		this.receiptCode = receiptCode;
	}

	public LocalDate getSalesDate() {
		return salesDate;
	}

	public void setSalesDate(LocalDate salesDate) {
		this.salesDate = salesDate;
	}

	public BigDecimal getSalesPrice() {
		return salesPrice;
	}

	public void setSalesPrice(BigDecimal salesPrice) {
		this.salesPrice = salesPrice;
	}

	public Integer getSalesAmount() {
		return salesAmount;
	}

	public void setSalesAmount(Integer salesAmount) {
		this.salesAmount = salesAmount;
	}

	public ReceiptType getReceiptType() {
		return receiptType;
	}

	public void setReceiptType(ReceiptType receiptType) {
		this.receiptType = receiptType;
	}

	public Representative getRepresentative() {
		return representative;
	}

	public void setRepresentative(Representative representative) {
		this.representative = representative;
	}

	public Artisanship getArtisanship() {
		return artisanship;
	}

	public void setArtisanship(Artisanship artisanship) {
		this.artisanship = artisanship;
	}

}