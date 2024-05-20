package org.example.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Base64;

@Controller
public class FileSenderController {

    private static final String SERVER_URL = "http://localhost:8081/upload";
    private static final int BATCH_SIZE = 16;

    @GetMapping("/send-file")
    public ResponseEntity<String> sendFile() throws IOException {
        File file = new File("/Users/pavelstalev/Desktop/Hello.txt");
        FileInputStream fis = new FileInputStream(file);
        byte[] buffer = new byte[BATCH_SIZE];
        int bytesRead;
        int partNumber = 0;

        long totalParts = (file.length() + BATCH_SIZE - 1) / BATCH_SIZE;

        RestTemplate restTemplate = new RestTemplate();

        while ((bytesRead = fis.read(buffer)) != -1) {
            byte[] chunk = new byte[bytesRead];
            System.arraycopy(buffer, 0, chunk, 0, bytesRead);
            String encodedChunk = Base64.getEncoder().encodeToString(chunk);

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-File-Part", encodedChunk);
            headers.add("X-Part-Number", String.valueOf(partNumber));
            headers.add("X-File-Name", file.getName());
            headers.add("X-Total-Parts", String.valueOf(totalParts));

            HttpEntity<String> requestEntity = new HttpEntity<>(null, headers);
            restTemplate.postForEntity(SERVER_URL, requestEntity, String.class);
            partNumber++;
        }

        fis.close();
        return ResponseEntity.ok("File sent successfully");
    }
}

