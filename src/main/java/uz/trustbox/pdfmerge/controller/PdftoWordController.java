package uz.trustbox.pdfmerge.controller;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping("/api/convert")
public class PdftoWordController {


        @PostMapping("/pdf-to-word")
        public ResponseEntity<byte[]> convertPdfToWord(@RequestParam("file") MultipartFile file) {
            try (PDDocument pdfDocument = PDDocument.load(file.getInputStream());
                 XWPFDocument wordDocument = new XWPFDocument();
                 ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

                // Extract text from PDF
                PDFTextStripper textStripper = new PDFTextStripper();
                String pdfText = textStripper.getText(pdfDocument);

                // Write text to Word
                XWPFParagraph paragraph = wordDocument.createParagraph();
                paragraph.createRun().setText(pdfText);
                wordDocument.write(outputStream);

                // Return Word file as response
                HttpHeaders headers = new HttpHeaders();
                headers.add("Content-Disposition", "attachment; filename=converted.docx");

                return new ResponseEntity<>(outputStream.toByteArray(), headers, HttpStatus.OK);

            } catch (IOException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
            }
        }
    }


