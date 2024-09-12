package com.example.bankdkistock.repository;

import com.example.bankdkistock.model.Stock;

import java.util.List;
import java.util.Optional;

public interface StockRepository {
    Stock save(Stock stock);

    Optional<Stock> findById(Long id);

    List<Stock> findAll();

    void delete(Stock stock);
}