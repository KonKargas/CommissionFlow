package com.kkargas.sales.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;

@Entity
public class Artisanship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long artisanshipId;

    private String artisanshipName;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "address_id")
    private Address artisanshipAddress;

    public Artisanship() {
    }

	public Long getArtisanshipId() {
		return artisanshipId;
	}

	public void setArtisanshipId(Long artisanshipId) {
		this.artisanshipId = artisanshipId;
	}

	public String getArtisanshipName() {
		return artisanshipName;
	}

	public void setArtisanshipName(String artisanshipName) {
		this.artisanshipName = artisanshipName;
	}

	public Address getArtisanshipAddress() {
		return artisanshipAddress;
	}

	public void setArtisanshipAddress(Address artisanshipAddress) {
		this.artisanshipAddress = artisanshipAddress;
	}

}