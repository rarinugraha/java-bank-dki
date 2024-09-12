package com.example.bankdkistock.dto;

import lombok.Builder;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
@Builder
public class RequestStockDTO {
    private Long id;
    private String namaBarang;
    private Integer jumlahStok;
    private String nomorSeriBarang;
    private String additionalInfo;
    private MultipartFile gambarBarang;
}