package com.example.ocr;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Service
public class PDFTextExtractorService {

    @Value("${tess4j.datapath}")
    private String tessDataPath;

    public String extractText(File file) throws IOException {
        StringBuilder result = new StringBuilder();

        try (PDDocument document = PDDocument.load(file)) {
            int totalPages = document.getNumberOfPages();

            PDFTextStripper stripper = new PDFTextStripper();
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");

            PDFRenderer renderer = new PDFRenderer(document);

            for (int page = 0; page < totalPages; page++) {
                // Process one page at a time to save memory
                stripper.setStartPage(page + 1);
                stripper.setEndPage(page + 1);

                String pageText = stripper.getText(document).trim();
                result.append("===== Page ").append(page + 1).append(" - Extracted Text =====\n");
                result.append(pageText).append("\n");

                try {
                    BufferedImage image = renderer.renderImageWithDPI(page, 300);
                    String ocrText = tesseract.doOCR(image);
                    result.append("===== Page ").append(page + 1).append(" - OCR Text =====\n");
                    result.append(ocrText).append("\n\n");
                } catch (TesseractException e) {
                    result.append("OCR failed on page ").append(page + 1).append(": ").append(e.getMessage()).append("\n");
                }
            }
        }

        return result.toString();
    }



}