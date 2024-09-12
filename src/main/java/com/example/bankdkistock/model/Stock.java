package com.example.bankdkistock.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "stocks")
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "stock_seq")
    @SequenceGenerator(name = "stock_seq", sequenceName = "stock_seq", allocationSize = 1)
    private Long id;

    @Column(name = "nama_barang", nullable = false)
    private String namaBarang;

    @Column(name = "jumlah_stok", nullable = false)
    private int jumlahStok;

    @Column(name = "nomor_seri_barang", nullable = false, unique = true)
    private String nomorSeriBarang;

    @Column(name = "additional_info", columnDefinition = "jsonb")
    private String additionalInfo;

    @Column(name = "gambar_barang")
    private String gambarBarang;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "updated_by")
    private Long updatedBy;
}
