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
            // 1. Extract embedded text from PDF
            PDFTextStripper stripper = new PDFTextStripper();
            String directText = stripper.getText(document).trim();

            result.append("=== TEXT from PDF (Direct) ===\n");
            if (!directText.isEmpty()) {
                result.append(directText).append("\n\n");
            } else {
                result.append("[No embedded PDF text found]\n\n");
            }

            // 2. OCR setup
            PDFRenderer pdfRenderer = new PDFRenderer(document);
            Tesseract tesseract = new Tesseract();
            tesseract.setDatapath(tessDataPath);
            tesseract.setLanguage("eng");

            // Maximum accuracy: LSTM-only, automatic layout
            tesseract.setOcrEngineMode(1); // OEM 1 = LSTM
            tesseract.setPageSegMode(3);   // PSM 3 = fully automatic page segmentation

            result.append("=== OCR from Images ===\n");

            for (int page = 0; page < document.getNumberOfPages(); ++page) {
                // Step 1: Render PDF page at 600 DPI
                BufferedImage image = pdfRenderer.renderImageWithDPI(page, 600);

                // Step 2: Convert to grayscale
                BufferedImage grayImage = new BufferedImage(
                        image.getWidth(), image.getHeight(),
                        BufferedImage.TYPE_BYTE_GRAY
                );
                grayImage.getGraphics().drawImage(image, 0, 0, null);

                // Step 3: Apply binary thresholding (simple binarization)
                BufferedImage binarized = new BufferedImage(
                        grayImage.getWidth(), grayImage.getHeight(),
                        BufferedImage.TYPE_BYTE_BINARY
                );
                binarized.getGraphics().drawImage(grayImage, 0, 0, null);

                // Step 4: Run OCR
                String ocrText = tesseract.doOCR(binarized);

                result.append("Page ").append(page + 1).append(":\n")
                        .append(ocrText).append("\n\n");
            }

        } catch (TesseractException e) {
            e.printStackTrace();
            result.append("OCR failed: ").append(e.getMessage());
        }

        return result.toString();
    }


}