package com.example.bankdkistock.controller;

import com.example.bankdkistock.dto.ApiResponse;
import com.example.bankdkistock.dto.RequestStockDTO;
import com.example.bankdkistock.dto.ResponseStockDTO;
import com.example.bankdkistock.service.StockService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/stocks")
public class StockController {

    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Object>> createStock(@ModelAttribute RequestStockDTO requestStockDTO) {
        try {
            Object result = stockService.createStock(requestStockDTO);
            return ResponseEntity.ok(new ApiResponse<>("success", "Stock created successfully", result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("failed", e.getMessage(), null));
        }
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ResponseStockDTO>>> listAllStocks() {
        List<ResponseStockDTO> stocks = stockService.listAllStocks();
        return ResponseEntity.ok(new ApiResponse<>("success", "Stocks retrieved successfully", stocks));
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Object>> updateStock(@PathVariable Long id, @ModelAttribute RequestStockDTO requestStockDTO) {
        try {
            ResponseStockDTO result = stockService.updateStock(id, requestStockDTO);
            String message = result == null ? "Data not found" : "Stock updated successfully";
            return ResponseEntity.ok(new ApiResponse<>("success", message, result));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ApiResponse<>("failed", e.getMessage(), null));
        }
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<ResponseStockDTO>> getStockDetail(@PathVariable Long id) {
        ResponseStockDTO stock = stockService.getStockById(id);
        String message = stock == null ? "Data not found" : "Stock retrieved successfully";
        return ResponseEntity.ok(new ApiResponse<>("success", message, stock));
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteStock(@PathVariable Long id) {
        boolean isDeleted = stockService.deleteStock(id);
        String message = isDeleted ? "Stock deleted successfully" : "Data not found";
        return ResponseEntity.ok(new ApiResponse<>("success", message, null));
    }
}
