package com.lmserver.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.List;

/**
 * Google Sheets 服务 - 读写 Google 电子表格，服务账号懒加载认证。
 */
@Slf4j
@Service
public class GoogleSheetsService {

    @Value("${google.sheets.credentials-path}")
    private String credentialsPath;

    private Sheets sheets;
    private boolean initialized;

    private synchronized void ensureInitialized() {
        if (initialized) return;
        if (!new java.io.File(credentialsPath).exists()) {
            log.warn("Google Sheets credentials not found: {}", credentialsPath);
            initialized = true;
            return;
        }
        try {
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(credentialsPath))
                    .createScoped("https://www.googleapis.com/auth/spreadsheets");
            sheets = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("LM-Server")
                    .build();
            log.info("Google Sheets initialized");
        } catch (Exception e) {
            log.error("Google Sheets init failed: {}", e.getMessage());
        }
        initialized = true;
    }

    public List<List<Object>> read(String spreadsheetId, String range) throws Exception {
        ensureInitialized();
        if (sheets == null) throw new RuntimeException("Sheets not initialized");
        return sheets.spreadsheets().values().get(spreadsheetId, range).execute().getValues();
    }

    public void write(String spreadsheetId, String range, List<List<Object>> values) throws Exception {
        ensureInitialized();
        if (sheets == null) throw new RuntimeException("Sheets not initialized");
        sheets.spreadsheets().values()
                .update(spreadsheetId, range, new com.google.api.services.sheets.v4.model.ValueRange().setValues(values))
                .setValueInputOption("USER_ENTERED").execute();
    }
}
