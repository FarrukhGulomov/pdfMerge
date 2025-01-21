package uz.trustbox.pdfmerge.controller;

import org.apache.pdfbox.multipdf.PDFMergerUtility;
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
@RequestMapping("/api/pdf")
public class PdfController {
    @PostMapping("/merge")
    public ResponseEntity<byte[]> mergePdfs(@RequestParam("files")MultipartFile[] files){
        if(files.length<2){
            return ResponseEntity.badRequest().body(null);
        }
        try(ByteArrayOutputStream outputStream=new ByteArrayOutputStream()){
            PDFMergerUtility pdfMerger=new PDFMergerUtility();
            pdfMerger.setDestinationStream(outputStream);
            for(MultipartFile file:files){
                pdfMerger.addSource(file.getInputStream());
            }
            pdfMerger.mergeDocuments(null);
            HttpHeaders headers=new HttpHeaders();
            headers.add("Content-Disposition","attachment; filename=merged.pdf");
            return new ResponseEntity<>(outputStream.toByteArray(),headers, HttpStatus.OK);

        }
        catch (IOException e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
