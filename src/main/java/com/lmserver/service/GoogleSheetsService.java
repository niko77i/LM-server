package com.lmserver.service;

import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.util.List;

/**
 * Google Sheets 服务 — 读写 Google 电子表格。
 * 使用服务账号凭证认证（从 GG-Server 拷贝）。
 */
@Slf4j
@Service
public class GoogleSheetsService {

    @Value("${google.sheets.credentials-path}")
    private String credentialsPath;

    private Sheets sheets;

    @PostConstruct
    public void init() {
        try {
            if (!new java.io.File(credentialsPath).exists()) {
                log.warn("Google Sheets 凭证文件不存在: {}，跳过初始化", credentialsPath);
                return;
            }
            GoogleCredentials credentials = GoogleCredentials
                    .fromStream(new FileInputStream(credentialsPath))
                    .createScoped("https://www.googleapis.com/auth/spreadsheets");
            sheets = new Sheets.Builder(
                    GoogleNetHttpTransport.newTrustedTransport(),
                    GsonFactory.getDefaultInstance(),
                    new HttpCredentialsAdapter(credentials))
                    .setApplicationName("LM-Server")
                    .build();
            log.info("Google Sheets 服务初始化完成");
        } catch (Exception e) {
            log.error("Google Sheets 初始化失败: {}", e.getMessage());
        }
    }

    /** 读取 Sheet 指定范围的数据 */
    public List<List<Object>> read(String spreadsheetId, String range) throws Exception {
        ValueRange result = sheets.spreadsheets().values()
                .get(spreadsheetId, range).execute();
        return result.getValues();
    }

    /** 写入数据到指定范围 */
    public void write(String spreadsheetId, String range, List<List<Object>> values) throws Exception {
        ValueRange body = new ValueRange().setValues(values);
        sheets.spreadsheets().values()
                .update(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }

    /** 追加数据到 Sheet 末尾 */
    public void append(String spreadsheetId, String range, List<List<Object>> values) throws Exception {
        ValueRange body = new ValueRange().setValues(values);
        sheets.spreadsheets().values()
                .append(spreadsheetId, range, body)
                .setValueInputOption("USER_ENTERED")
                .execute();
    }
}
