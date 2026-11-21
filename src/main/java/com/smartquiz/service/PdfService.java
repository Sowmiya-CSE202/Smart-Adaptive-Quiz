package com.smartquiz.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class PdfService {

    public String extract(MultipartFile file) throws IOException {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException(
                    "Please select a PDF file."
            );
        }

        String name = file.getOriginalFilename() == null
                ? ""
                : file.getOriginalFilename().toLowerCase();

        if (!name.endsWith(".pdf")) {
            throw new IllegalArgumentException(
                    "Only PDF files are supported."
            );
        }

        try (PDDocument doc =
                     Loader.loadPDF(file.getBytes())) {

            String text =
                    new PDFTextStripper().getText(doc);

            if (text == null || text.isBlank()) {

                throw new IllegalArgumentException(
                        "No readable text found. The PDF may be scanned or image-only."
                );
            }

            return text;
        }
    }
}
