package com.example.bankdkistock.controller;

import com.example.bankdkistock.dto.ApiResponse;
import com.example.bankdkistock.dto.RequestStockDTO;
import com.example.bankdkistock.dto.ResponseStockDTO;
import com.example.bankdkistock.service.StockService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/stocks")
public class StockController {

    private final Logger logger = LogManager.getLogger(StockController.class);

    @Autowired
    private StockService stockService;

    @PostMapping("/create")
    public ResponseEntity<ApiResponse<Object>> createStock(@ModelAttribute RequestStockDTO requestStockDTO) {
        logger.info("createStock request: {}", requestStockDTO);
        ResponseEntity<ApiResponse<Object>> response = handleServiceCall(() -> stockService.createStock(requestStockDTO), "Stock created successfully");
        logger.info("createStock response: {}", response);
        return response;
    }

    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<ResponseStockDTO>>> listAllStocks() {
        logger.info("listAllStocks request");
        List<ResponseStockDTO> stocks = stockService.listAllStocks();
        ResponseEntity<ApiResponse<List<ResponseStockDTO>>> response = ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Stocks retrieved successfully", stocks)
        );
        logger.info("listAllStocks response: {}", response);
        return response;
    }

    @GetMapping("/detail/{id}")
    public ResponseEntity<ApiResponse<ResponseStockDTO>> getStockDetail(@PathVariable Long id) {
        logger.info("getStockDetail request for ID: {}", id);
        ResponseEntity<ApiResponse<ResponseStockDTO>> response = ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Stock retrieved successfully", stockService.getStockById(id))
        );
        logger.info("getStockDetail response: {}", response);
        return response;
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<ApiResponse<Object>> updateStock(@PathVariable Long id, @ModelAttribute RequestStockDTO requestStockDTO) {
        logger.info("updateStock request for ID: {}, data: {}", id, requestStockDTO);
        ResponseEntity<ApiResponse<Object>> response = handleServiceCall(() -> stockService.updateStock(id, requestStockDTO), "Stock updated successfully");
        logger.info("updateStock response: {}", response);
        return response;
    }

    @DeleteMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Object>> deleteStock(@PathVariable Long id) {
        logger.info("deleteStock request for ID: {}", id);
        stockService.deleteStock(id);
        ResponseEntity<ApiResponse<Object>> response = ResponseEntity.ok(
                new ApiResponse<>(HttpStatus.OK.value(), "Stock deleted successfully", null)
        );
        logger.info("deleteStock response: {}", response);
        return response;
    }

    // Helper method to handle service calls and error handling
    private ResponseEntity<ApiResponse<Object>> handleServiceCall(ServiceCall call, String successMessage) {
        try {
            Object result = call.execute();
            ApiResponse<Object> response = new ApiResponse<>(HttpStatus.OK.value(), successMessage, result);
            logger.info("Response: {}", response);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ApiResponse<Object> response = new ApiResponse<>(HttpStatus.BAD_REQUEST.value(), e.getMessage(), null);
            logger.error("Error: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(response);
        }
    }

    @FunctionalInterface
    private interface ServiceCall {
        Object execute() throws Exception;
    }
}
