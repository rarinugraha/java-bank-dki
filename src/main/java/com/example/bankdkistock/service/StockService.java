package com.example.bankdkistock.service;

import com.example.bankdkistock.dto.ResponseStockDTO;
import com.example.bankdkistock.dto.RequestStockDTO;
import com.example.bankdkistock.model.Stock;
import com.example.bankdkistock.model.User;
import com.example.bankdkistock.repository.StockRepository;
import com.example.bankdkistock.util.AuthenticatedUserUtil;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class StockService {

    private final StockRepository stockRepository;
    private final UserService userService;
    private final AuthenticatedUserUtil authenticatedUserUtil;

    public StockService(StockRepository stockRepository, UserService userService, AuthenticatedUserUtil authenticatedUserUtil) {
        this.stockRepository = stockRepository;
        this.userService = userService;
        this.authenticatedUserUtil = authenticatedUserUtil;
    }

    public ResponseStockDTO createStock(RequestStockDTO requestStockDTO) throws Exception {
        String username = authenticatedUserUtil.getAuthenticatedUsername();
        User currentUser = userService.findByUsername(username);

        Stock stock = Stock.builder()
                .namaBarang(requestStockDTO.getNamaBarang())
                .jumlahStok(requestStockDTO.getJumlahStok())
                .nomorSeriBarang(requestStockDTO.getNomorSeriBarang())
                .additionalInfo(requestStockDTO.getAdditionalInfo())
                .createdAt(LocalDateTime.now())
                .createdBy(currentUser.getId())
                .build();

        if (requestStockDTO.getGambarBarang() != null && !requestStockDTO.getGambarBarang().isEmpty()) {
            validateImage(requestStockDTO.getGambarBarang());
            String imagePath = saveImage(requestStockDTO.getGambarBarang());
            stock.setGambarBarang(imagePath);
        }

        try {
            Stock savedStock = stockRepository.save(stock);
            return convertToDTO(savedStock);
        } catch (DataIntegrityViolationException ex) {
            if (ex.getMessage().contains("uc_stock_nomorseribarang")) {
                throw new Exception("The 'Nomor Seri Barang' must be unique. The value '" + requestStockDTO.getNomorSeriBarang() + "' already exists.");
            }

            throw new Exception("An error occurred while saving the stock.");
        }
    }

    public List<ResponseStockDTO> listAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public Stock updateStock(Long id, RequestStockDTO requestStockDTO) throws Exception {
        String username = authenticatedUserUtil.getAuthenticatedUsername();
        User currentUser = userService.findByUsername(username);

        Optional<Stock> existingStockOptional = stockRepository.findById(id);
        if (existingStockOptional.isEmpty()) return null;

        Stock existingStock = existingStockOptional.get();
        updateStockDetails(existingStock, requestStockDTO, currentUser);

        return stockRepository.save(existingStock);
    }

    public ResponseStockDTO getStockById(Long id) {
        return stockRepository.findById(id)
                .map(this::convertToDTO)
                .orElse(null);
    }

    public boolean deleteStock(Long id) {
        return stockRepository.findById(id)
                .map(stock -> {
                    if (stock.getGambarBarang() != null) {
                        deleteOldImage(stock.getGambarBarang());
                    }

                    stockRepository.delete(stock);

                    return true;
                }).orElse(false);
    }

    private ResponseStockDTO convertToDTO(Stock stock) {
        return ResponseStockDTO.builder()
                .id(stock.getId())
                .namaBarang(stock.getNamaBarang())
                .jumlahStok(stock.getJumlahStok())
                .nomorSeriBarang(stock.getNomorSeriBarang())
                .additionalInfo(stock.getAdditionalInfo())
                .gambarBarang(stock.getGambarBarang())
                .createdBy(stock.getCreatedBy())
                .createdAt(stock.getCreatedAt())
                .updatedBy(stock.getUpdatedBy())
                .updatedAt(stock.getUpdatedAt())
                .build();
    }

    private void updateStockDetails(Stock existingStock, RequestStockDTO requestStockDTO, User currentUser) throws Exception {
        existingStock.setNamaBarang(requestStockDTO.getNamaBarang());
        existingStock.setJumlahStok(requestStockDTO.getJumlahStok());
        existingStock.setNomorSeriBarang(requestStockDTO.getNomorSeriBarang());
        existingStock.setAdditionalInfo(requestStockDTO.getAdditionalInfo());
        existingStock.setUpdatedAt(LocalDateTime.now());
        existingStock.setUpdatedBy(currentUser.getId());

        if (requestStockDTO.getGambarBarang() != null && !requestStockDTO.getGambarBarang().isEmpty()) {
            validateImage(requestStockDTO.getGambarBarang());
            String newImagePath = saveImage(requestStockDTO.getGambarBarang());

            if (existingStock.getGambarBarang() != null) {
                deleteOldImage(existingStock.getGambarBarang());
            }

            existingStock.setGambarBarang(newImagePath);
        }
    }

    private void validateImage(MultipartFile imageFile) throws Exception {
        String contentType = imageFile.getContentType();
        if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
            throw new Exception("Only JPG and PNG images are allowed");
        }
    }

    private String saveImage(MultipartFile imageFile) throws IOException {
        String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        Path uploadPath = Paths.get("src/main/resources/static/uploads/");
        if (!Files.exists(uploadPath)) Files.createDirectories(uploadPath);

        Path filePath = uploadPath.resolve(fileName);
        try (InputStream inputStream = imageFile.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return "/uploads/" + fileName;
    }

    private void deleteOldImage(String oldImagePath) {
        Path oldImageFile = Paths.get("src/main/resources/static" + oldImagePath);
        try {
            Files.deleteIfExists(oldImageFile);
        } catch (IOException e) {
            System.err.println("Failed to delete old image: " + e.getMessage());
        }
    }
}
