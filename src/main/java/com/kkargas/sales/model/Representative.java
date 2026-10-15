package com.kkargas.sales.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;

@Entity
public class Representative {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long repId;

    private String fullName;

    @Column(unique = true, nullable = false)
    private String vatNumber;

    @OneToMany(mappedBy = "representative", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Receipt> receipts = new ArrayList<>();

    public Representative() {
    }

	public Long getRepId() {
		return repId;
	}

	public void setRepId(Long repId) {
		this.repId = repId;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getVatNumber() {
		return vatNumber;
	}

	public void setVatNumber(String vatNumber) {
		this.vatNumber = vatNumber;
	}

	public List<Receipt> getReceipts() {
		return receipts;
	}

	public void setReceipts(List<Receipt> receipts) {
		this.receipts = receipts;
	}

}