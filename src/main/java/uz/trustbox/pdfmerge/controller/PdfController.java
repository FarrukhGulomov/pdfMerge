package uz.trustbox.pdfmerge.controller;
import uz.trustbox.pdfmerge.exception.ErrorResponse;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/pdf")
public class PdfController {
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final long MAX_TOTAL_SIZE = 50 * 1024 * 1024; // 50MB

    @PostMapping("/merge")
    public ResponseEntity<?> mergePdfs(@RequestParam("files") MultipartFile[] files) {
        try {
            // Validate input
            if (files == null || files.length < 2) {
                return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("At least 2 PDF files are required"));
            }

            // Validate total size
            long totalSize = 0;
            for (MultipartFile file : files) {
                if (file.getSize() > MAX_FILE_SIZE) {
                    return ResponseEntity
                        .badRequest()
                        .body(new ErrorResponse("Individual file size cannot exceed 10MB"));
                }
                if (!file.getContentType().equals("application/pdf")) {
                    return ResponseEntity
                        .badRequest()
                        .body(new ErrorResponse("Only PDF files are allowed"));
                }
                totalSize += file.getSize();
            }

            if (totalSize > MAX_TOTAL_SIZE) {
                return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("Total file size cannot exceed 50MB"));
            }

            // Process files
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                PDFMergerUtility pdfMerger = new PDFMergerUtility();
                pdfMerger.setDestinationStream(outputStream);
                
                for (MultipartFile file : files) {
                    pdfMerger.addSource(file.getInputStream());
                }
                
                pdfMerger.mergeDocuments(null);
                
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Disposition", "attachment; filename=merged.pdf");
                headers.setContentType(MediaType.APPLICATION_PDF);
                
                return ResponseEntity
                    .ok()
                    .headers(headers)
                    .body(outputStream.toByteArray());
            }

        } catch (IOException e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Error processing files: " + e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Unexpected error: " + e.getMessage()));
        }
    }
}

