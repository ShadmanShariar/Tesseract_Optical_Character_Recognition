package com.example.ocr;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;

@RestController
@RequestMapping("/api/pdf")
public class PDFController {

    @Autowired
    private PDFTextExtractorService extractorService;

    @PostMapping(value = "/extract", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String extractTextFromPdf(@RequestParam("file") MultipartFile file) throws Exception {
        File temp = File.createTempFile("upload", ".pdf");
        file.transferTo(temp);
        String text = extractorService.extractText(temp);
        temp.delete(); // clean up
        return text;
    }
}
