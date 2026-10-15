package com.kkargas.sales.service;

import java.io.IOException;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.kkargas.sales.importer.HtmlReceiptImporter;
import com.kkargas.sales.importer.TxtReceiptImporter;
import com.kkargas.sales.importer.XmlReceiptImporter;

@Service
public class ImportService {

	private final TxtReceiptImporter txtReceiptImporter;
	private final XmlReceiptImporter xmlReceiptImporter;
	private final HtmlReceiptImporter htmlReceiptImporter;


	public ImportService(
	        TxtReceiptImporter txtReceiptImporter,
	        XmlReceiptImporter xmlReceiptImporter,
	        HtmlReceiptImporter htmlReceiptImporter) {

	    this.txtReceiptImporter =
	            txtReceiptImporter;

	    this.xmlReceiptImporter =
	            xmlReceiptImporter;

	    this.htmlReceiptImporter =
	            htmlReceiptImporter;
	}


    public int importFile(
            MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {

            throw new IllegalArgumentException(
                    "Please select a file to import."
            );
        }


        String filename =
                file.getOriginalFilename();


        if (filename == null
                || filename.isBlank()) {

            throw new IllegalArgumentException(
                    "Invalid file."
            );
        }


        String lowerName =
                filename.toLowerCase();


        if (lowerName.endsWith(".txt")) {

            return txtReceiptImporter
                    .importFile(file);
        }


        if (lowerName.endsWith(".xml")) {

            return xmlReceiptImporter
                    .importFile(file);
        }


        if (lowerName.endsWith(".html")
                || lowerName.endsWith(".htm")) {

            return htmlReceiptImporter
                    .importFile(file);
        }


        throw new IllegalArgumentException(
                "Unsupported file type. Use TXT, XML or HTML."
        );
    }
}