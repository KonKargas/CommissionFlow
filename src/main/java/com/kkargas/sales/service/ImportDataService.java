package com.kkargas.sales.service;

import java.math.BigDecimal;
import java.util.Objects;

import org.springframework.stereotype.Service;

import com.kkargas.sales.model.Address;
import com.kkargas.sales.model.Artisanship;
import com.kkargas.sales.model.Receipt;
import com.kkargas.sales.model.Representative;

@Service
public class ImportDataService {

    private final RepresentativeService representativeService;
    private final ArtisanshipService artisanshipService;
    private final ReceiptService receiptService;


    public ImportDataService(
            RepresentativeService representativeService,
            ArtisanshipService artisanshipService,
            ReceiptService receiptService) {

        this.representativeService = representativeService;
        this.artisanshipService = artisanshipService;
        this.receiptService = receiptService;
    }


    public Representative findOrCreateRepresentative(
            String fullName,
            String vatNumber) {

        Representative existing =
                representativeService
                        .getAllRepresentatives()
                        .stream()
                        .filter(rep ->
                                rep.getVatNumber() != null
                                && rep.getVatNumber()
                                      .equals(vatNumber)
                        )
                        .findFirst()
                        .orElse(null);

        if (existing != null) {
            return existing;
        }

        Representative representative =
                new Representative();

        representative.setFullName(fullName);
        representative.setVatNumber(vatNumber);

        return representativeService
                .saveRepresentative(representative);
    }


    public Artisanship findOrCreateArtisanship(
            String companyName,
            String country,
            String city,
            String street,
            String number) {

        Integer addressNumber =
                number == null || number.isBlank()
                        ? null
                        : Integer.valueOf(number);

        Artisanship existing =
                artisanshipService
                        .getAllArtisanships()
                        .stream()
                        .filter(a ->
                                sameArtisanship(
                                        a,
                                        companyName,
                                        country,
                                        city,
                                        street,
                                        addressNumber
                                )
                        )
                        .findFirst()
                        .orElse(null);

        if (existing != null) {
            return existing;
        }

        Address address =
                new Address();

        address.setCountry(country);
        address.setCity(city);
        address.setStreet(street);
        address.setNumber(addressNumber);


        Artisanship artisanship =
                new Artisanship();

        artisanship.setArtisanshipName(companyName);
        artisanship.setArtisanshipAddress(address);

        return artisanshipService
                .saveArtisanship(artisanship);
    }


    public boolean sameReceipt(
            Receipt first,
            Receipt second) {

        if (!Objects.equals(
                first.getSalesDate(),
                second.getSalesDate())) {

            return false;
        }

        if (first.getReceiptType()
                != second.getReceiptType()) {

            return false;
        }

        if (!Objects.equals(
                first.getSalesAmount(),
                second.getSalesAmount())) {

            return false;
        }

        if (!samePrice(
                first.getSalesPrice(),
                second.getSalesPrice())) {

            return false;
        }

        if (first.getRepresentative() == null
                || second.getRepresentative() == null
                || !Objects.equals(
                        first.getRepresentative().getVatNumber(),
                        second.getRepresentative().getVatNumber())) {

            return false;
        }

        if (first.getArtisanship() == null
                || second.getArtisanship() == null
                || !Objects.equals(
                        first.getArtisanship().getArtisanshipId(),
                        second.getArtisanship().getArtisanshipId())) {

            return false;
        }

        return true;
    }


    public Receipt getExistingReceipt(Long code) {

        return receiptService
                .getReceiptByCode(code);
    }


    public Long getMaxReceiptCode() {

        Long max =
                receiptService.getMaxReceiptCode();

        return max == null
                ? 0L
                : max;
    }


    public void saveReceipt(Receipt receipt) {

        receiptService
                .saveReceipt(receipt);
    }


    private boolean samePrice(
            BigDecimal first,
            BigDecimal second) {

        if (first == null && second == null) {
            return true;
        }

        if (first == null || second == null) {
            return false;
        }

        return first.compareTo(second) == 0;
    }


    private boolean sameArtisanship(
            Artisanship artisanship,
            String companyName,
            String country,
            String city,
            String street,
            Integer number) {

        if (artisanship == null
                || artisanship.getArtisanshipAddress() == null) {

            return false;
        }

        Address address =
                artisanship.getArtisanshipAddress();

        return sameText(
                    artisanship.getArtisanshipName(),
                    companyName
                )
                && sameText(
                    address.getCountry(),
                    country
                )
                && sameText(
                    address.getCity(),
                    city
                )
                && sameText(
                    address.getStreet(),
                    street
                )
                && Objects.equals(
                    address.getNumber(),
                    number
                );
    }


    private boolean sameText(
            String first,
            String second) {

        String a =
                first == null
                        ? ""
                        : first.trim();

        String b =
                second == null
                        ? ""
                        : second.trim();

        return a.equalsIgnoreCase(b);
    }
}