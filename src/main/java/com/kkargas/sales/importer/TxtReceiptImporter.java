package com.kkargas.sales.importer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kkargas.sales.enums.ReceiptType;
import com.kkargas.sales.model.Address;
import com.kkargas.sales.model.Artisanship;
import com.kkargas.sales.model.Receipt;
import com.kkargas.sales.model.Representative;
import com.kkargas.sales.service.ArtisanshipService;
import com.kkargas.sales.service.ReceiptService;
import com.kkargas.sales.service.RepresentativeService;


@Component
public class TxtReceiptImporter implements ReceiptImporter {

    private final ReceiptService receiptService;
    private final RepresentativeService representativeService;
    private final ArtisanshipService artisanshipService;


    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("d/M/yyyy");


    public TxtReceiptImporter(
            ReceiptService receiptService,
            RepresentativeService representativeService,
            ArtisanshipService artisanshipService) {

        this.receiptService = receiptService;
        this.representativeService = representativeService;
        this.artisanshipService = artisanshipService;
    }


    // =========================================================
    // IMPORT
    // =========================================================

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int importFile(MultipartFile file) throws IOException {

        try (BufferedReader reader =
                     new BufferedReader(
                             new InputStreamReader(
                                     file.getInputStream(),
                                     StandardCharsets.UTF_8
                             )
                     )) {


            // =================================================
            // REPRESENTATIVE DATA
            // =================================================

            String nameLine = reader.readLine();
            String afmLine = reader.readLine();
            String receiptsLine = reader.readLine();


            if (nameLine == null
                    || afmLine == null
                    || receiptsLine == null) {

                throw new IllegalArgumentException(
                        "Invalid TXT file structure."
                );
            }


            String representativeName =
                    getValue(nameLine);

            String vatNumber =
                    getValue(afmLine);


            Representative representative =
                    findOrCreateRepresentative(
                            representativeName,
                            vatNumber
                    );


            // =================================================
            // IMPORT STATE
            // =================================================

            int importedCount = 0;

            /*
             * Receipts whose code already exists,
             * but whose contents are different.
             *
             * These will be imported at the end
             * with newly generated receipt codes.
             */
            List<Receipt> conflictingReceipts =
                    new ArrayList<>();


            Long highestReceiptCode =
                    receiptService.getMaxReceiptCode();


            if (highestReceiptCode == null) {
                highestReceiptCode = 0L;
            }


            String line;


            // =================================================
            // RECEIPTS
            // =================================================

            while ((line = reader.readLine()) != null) {

                if (line.isBlank()) {
                    continue;
                }


                if (!line.startsWith("Receipt ID:")) {
                    continue;
                }


                // =============================================
                // READ RECEIPT DATA
                // =============================================

                Long receiptCode =
                        Long.valueOf(
                                getValue(line)
                        );


                String date =
                        getValue(reader.readLine());

                String kind =
                        getValue(reader.readLine());

                String sales =
                        getValue(reader.readLine());

                String items =
                        getValue(reader.readLine());

                String company =
                        getValue(reader.readLine());

                String country =
                        getValue(reader.readLine());

                String city =
                        getValue(reader.readLine());

                String street =
                        getValue(reader.readLine());

                String number =
                        getValue(reader.readLine());


                // =============================================
                // FIND / CREATE ARTISANSHIP
                // =============================================

                Artisanship artisanship =
                        findOrCreateArtisanship(
                                company,
                                country,
                                city,
                                street,
                                number
                        );


                // =============================================
                // BUILD RECEIPT
                // =============================================

                Receipt receipt =
                        new Receipt();


                receipt.setReceiptCode(
                        receiptCode
                );


                receipt.setSalesDate(
                        LocalDate.parse(
                                date,
                                DATE_FORMAT
                        )
                );


                receipt.setReceiptType(
                        parseReceiptType(kind)
                );


                receipt.setSalesPrice(
                        new BigDecimal(sales)
                );


                receipt.setSalesAmount(
                        Integer.valueOf(items)
                );


                receipt.setRepresentative(
                        representative
                );


                receipt.setArtisanship(
                        artisanship
                );


                // =============================================
                // DUPLICATE / CONFLICT CHECK
                // =============================================

                Receipt existingReceipt =
                        receiptService
                                .getReceiptByCode(
                                        receiptCode
                                );


                /*
                 * CASE 1:
                 *
                 * The code does not exist.
                 *
                 * Save the receipt normally.
                 */
                if (existingReceipt == null) {

                    receiptService.saveReceipt(
                            receipt
                    );


                    highestReceiptCode =
                            Math.max(
                                    highestReceiptCode,
                                    receiptCode
                            );


                    importedCount++;

                    continue;
                }


                /*
                 * CASE 2:
                 *
                 * Same code AND same receipt data.
                 *
                 * It is a true duplicate.
                 * Do not import it again.
                 */
                if (sameReceipt(
                        existingReceipt,
                        receipt)) {

                    continue;
                }


                /*
                 * CASE 3:
                 *
                 * Same code but different receipt data.
                 *
                 * The receipt is valid,
                 * but its code conflicts with an existing one.
                 *
                 * Keep it temporarily and assign
                 * a new code after processing the file.
                 */
                conflictingReceipts.add(
                        receipt
                );
            }


            // =================================================
            // SAVE RECEIPTS WITH CONFLICTING CODES
            // =================================================

            for (Receipt conflictingReceipt
                    : conflictingReceipts) {

                highestReceiptCode++;


                conflictingReceipt.setReceiptCode(
                        highestReceiptCode
                );


                receiptService.saveReceipt(
                        conflictingReceipt
                );


                importedCount++;
            }


            return importedCount;
        }
    }


    // =========================================================
    // REPRESENTATIVE
    // =========================================================

    private Representative findOrCreateRepresentative(
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


        representative.setFullName(
                fullName
        );


        representative.setVatNumber(
                vatNumber
        );


        return representativeService
                .saveRepresentative(
                        representative
                );
    }


    // =========================================================
    // ARTISANSHIP
    // =========================================================

    private Artisanship findOrCreateArtisanship(
            String companyName,
            String country,
            String city,
            String street,
            String number) {


        Integer addressNumber =
                null;


        if (number != null
                && !number.isBlank()) {

            addressNumber =
                    Integer.valueOf(number);
        }


        final Integer finalAddressNumber =
                addressNumber;


        /*
         * We do NOT compare only the company name.
         *
         * Example:
         *
         * Adidas - Kalamata
         * Adidas - Patra
         *
         * These may be different artisanship locations.
         */
        Artisanship existing =
                artisanshipService
                        .getAllArtisanships()
                        .stream()
                        .filter(artisanship ->
                                sameArtisanship(
                                        artisanship,
                                        companyName,
                                        country,
                                        city,
                                        street,
                                        finalAddressNumber
                                )
                        )
                        .findFirst()
                        .orElse(null);


        if (existing != null) {
            return existing;
        }


        // =============================================
        // CREATE ADDRESS
        // =============================================

        Address address =
                new Address();


        address.setCountry(
                country
        );


        address.setCity(
                city
        );


        address.setStreet(
                street
        );


        address.setNumber(
                addressNumber
        );


        // =============================================
        // CREATE ARTISANSHIP
        // =============================================

        Artisanship artisanship =
                new Artisanship();


        artisanship.setArtisanshipName(
                companyName
        );


        artisanship.setArtisanshipAddress(
                address
        );


        return artisanshipService
                .saveArtisanship(
                        artisanship
                );
    }


    // =========================================================
    // ARTISANSHIP COMPARISON
    // =========================================================

    private boolean sameArtisanship(
            Artisanship artisanship,
            String companyName,
            String country,
            String city,
            String street,
            Integer number) {


        if (artisanship == null) {
            return false;
        }


        if (!sameText(
                artisanship.getArtisanshipName(),
                companyName)) {

            return false;
        }


        Address address =
                artisanship
                        .getArtisanshipAddress();


        if (address == null) {
            return false;
        }


        if (!sameText(
                address.getCountry(),
                country)) {

            return false;
        }


        if (!sameText(
                address.getCity(),
                city)) {

            return false;
        }


        if (!sameText(
                address.getStreet(),
                street)) {

            return false;
        }


        return Objects.equals(
                address.getNumber(),
                number
        );
    }


    // =========================================================
    // RECEIPT COMPARISON
    // =========================================================

    private boolean sameReceipt(
            Receipt first,
            Receipt second) {


        if (first == null
                || second == null) {

            return false;
        }


        // DATE

        if (!Objects.equals(
                first.getSalesDate(),
                second.getSalesDate())) {

            return false;
        }


        // TYPE

        if (first.getReceiptType()
                != second.getReceiptType()) {

            return false;
        }


        // NUMBER OF ITEMS

        if (!Objects.equals(
                first.getSalesAmount(),
                second.getSalesAmount())) {

            return false;
        }


        // SALES PRICE

        if (!samePrice(
                first.getSalesPrice(),
                second.getSalesPrice())) {

            return false;
        }


        // REPRESENTATIVE

        if (!sameRepresentative(
                first.getRepresentative(),
                second.getRepresentative())) {

            return false;
        }


        // ARTISANSHIP

        if (!sameArtisanshipEntity(
                first.getArtisanship(),
                second.getArtisanship())) {

            return false;
        }


        /*
         * We already found first by second.receiptCode,
         * therefore their receipt code is the same.
         *
         * If every other field also matches,
         * this is a true duplicate.
         */
        return true;
    }


    // =========================================================
    // PRICE COMPARISON
    // =========================================================

    private boolean samePrice(
            BigDecimal first,
            BigDecimal second) {


        if (first == null
                && second == null) {

            return true;
        }


        if (first == null
                || second == null) {

            return false;
        }


        /*
         * BigDecimal:
         *
         * 1000
         * 1000.0
         * 1000.00
         *
         * must be considered the same monetary value.
         */
        return first.compareTo(second) == 0;
    }


    // =========================================================
    // REPRESENTATIVE COMPARISON
    // =========================================================

    private boolean sameRepresentative(
            Representative first,
            Representative second) {


        if (first == null
                || second == null) {

            return first == second;
        }


        /*
         * VAT number is the natural identifier
         * of the representative.
         */
        return Objects.equals(
                first.getVatNumber(),
                second.getVatNumber()
        );
    }


    // =========================================================
    // ARTISANSHIP ENTITY COMPARISON
    // =========================================================

    private boolean sameArtisanshipEntity(
            Artisanship first,
            Artisanship second) {


        if (first == null
                || second == null) {

            return first == second;
        }


        /*
         * Since findOrCreateArtisanship returns
         * the database entity, IDs are enough here.
         */
        if (first.getArtisanshipId() != null
                && second.getArtisanshipId() != null) {

            return Objects.equals(
                    first.getArtisanshipId(),
                    second.getArtisanshipId()
            );
        }


        return sameText(
                first.getArtisanshipName(),
                second.getArtisanshipName()
        );
    }


    // =========================================================
    // TEXT COMPARISON
    // =========================================================

    private boolean sameText(
            String first,
            String second) {


        String firstValue =
                normalizeText(first);

        String secondValue =
                normalizeText(second);


        return firstValue
                .equalsIgnoreCase(
                        secondValue
                );
    }


    private String normalizeText(
            String value) {


        if (value == null) {
            return "";
        }


        return value
                .trim()
                .replaceAll("\\s+", " ");
    }


    // =========================================================
    // TXT VALUE
    // =========================================================

    private String getValue(
            String line) {


        if (line == null) {

            throw new IllegalArgumentException(
                    "Unexpected end of TXT file."
            );
        }


        int separator =
                line.indexOf(':');


        if (separator == -1) {

            throw new IllegalArgumentException(
                    "Invalid line: "
                            + line
            );
        }


        return line
                .substring(separator + 1)
                .trim();
    }


    // =========================================================
    // RECEIPT TYPE
    // =========================================================

    private ReceiptType parseReceiptType(
            String value) {


        return switch (
                value.trim()
                     .toLowerCase()
        ) {


            case "trousers" ->
                    ReceiptType.TROUSERS;


            case "skirts" ->
                    ReceiptType.SKIRTS;


            case "blouses",
                 "blouse",
                 "shirts",
                 "shirt" ->

                    ReceiptType.BLOUSES;


            case "coats",
                 "coat" ->

                    ReceiptType.COATS;


            default ->

                    throw new IllegalArgumentException(
                            "Unknown receipt type: "
                                    + value
                    );
        };
    }
}