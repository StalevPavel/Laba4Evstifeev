package org.example.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
@RequestMapping("/upload")
public class FileReceiverController {

    private Map<String, Map<Integer, byte[]>> filePartsMap = new ConcurrentHashMap<>();
    private Map<String, Integer> totalPartsMap = new ConcurrentHashMap<>();

    @PostMapping
    public ResponseEntity<String> receiveFilePart(@RequestHeader("X-File-Part") String filePart,
                                                  @RequestHeader("X-Part-Number") int partNumber,
                                                  @RequestHeader("X-File-Name") String fileName,
                                                  @RequestHeader("X-Total-Parts") int totalParts) throws IOException {

        byte[] decodedPart = Base64.getDecoder().decode(filePart);

        filePartsMap.computeIfAbsent(fileName, k -> new ConcurrentHashMap<>()).put(partNumber, decodedPart);
        totalPartsMap.putIfAbsent(fileName, totalParts);

        if (filePartsMap.get(fileName).size() == totalPartsMap.get(fileName)) {
            assembleFile(fileName);
        }

        return ResponseEntity.ok("Part received");
    }

    private void assembleFile(String fileName) throws IOException {
        Map<Integer, byte[]> parts = filePartsMap.get(fileName);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        for (int i = 0; i < parts.size(); i++) {
            outputStream.write(parts.get(i));
        }

        byte[] fileContent = outputStream.toByteArray();

        System.out.println("File content received:");
        System.out.println(new String(fileContent));

        try (FileOutputStream fos = new FileOutputStream("/Users/pavelstalev/Desktop/" + fileName)) {
            fos.write(fileContent);
        }

        outputStream.close();

        filePartsMap.remove(fileName);
        totalPartsMap.remove(fileName);
    }
}
