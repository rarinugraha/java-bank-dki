package com.example.bankdkistock.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ResponseStockDTO {
    private Long id;
    private String namaBarang;
    private Integer jumlahStok;
    private String nomorSeriBarang;
    private JsonNode additionalInfo;
    private String gambarBarang;
    private LocalDateTime createdAt;
    private Long createdBy;
    private LocalDateTime updatedAt;
    private Long updatedBy;
}