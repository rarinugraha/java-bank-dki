package com.example.bankdkistock.service;

import com.example.bankdkistock.dto.ResponseStockDTO;
import com.example.bankdkistock.exception.NotFoundException;
import com.example.bankdkistock.dto.RequestStockDTO;
import com.example.bankdkistock.model.Stock;
import com.example.bankdkistock.model.User;
import com.example.bankdkistock.repository.StockRepository;
import com.example.bankdkistock.util.AuthenticatedUserUtil;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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

    @Transactional
    public Stock createStock(RequestStockDTO requestStockDTO) throws Exception {
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

        MultipartFile imageFile = requestStockDTO.getGambarBarang();
        if (imageFile != null && !imageFile.isEmpty()) {
            String contentType = imageFile.getContentType();
            if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
                throw new Exception("Only JPG and PNG images are allowed");
            }

            String imagePath = saveImageToStaticFolder(imageFile);
            stock.setGambarBarang(imagePath);
        }

        return stockRepository.save(stock);
    }

    public List<ResponseStockDTO> listAllStocks() {
        return stockRepository.findAll().stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    public ResponseStockDTO getStockById(Long id) {
        Optional<Stock> stock = stockRepository.findById(id);
        return stock.map(this::convertToDTO)
                .orElseThrow(() -> new NotFoundException("Stock not found with id: " + id));
    }

    public Stock updateStock(Long id, RequestStockDTO requestStockDTO) throws Exception {
        String username = authenticatedUserUtil.getAuthenticatedUsername();
        User currentUser = userService.findByUsername(username);

        Stock existingStock = stockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Stock not found"));

        existingStock.setNamaBarang(requestStockDTO.getNamaBarang());
        existingStock.setJumlahStok(requestStockDTO.getJumlahStok());
        existingStock.setNomorSeriBarang(requestStockDTO.getNomorSeriBarang());
        existingStock.setAdditionalInfo(requestStockDTO.getAdditionalInfo());
        existingStock.setUpdatedAt(LocalDateTime.now());
        existingStock.setUpdatedBy(currentUser.getId());

        if (requestStockDTO.getGambarBarang() != null && !requestStockDTO.getGambarBarang().isEmpty()) {
            MultipartFile imageFile = requestStockDTO.getGambarBarang();
            String contentType = imageFile.getContentType();
            if (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType)) {
                throw new Exception("Only JPG and PNG images are allowed");
            }

            String newImagePath = saveImageToStaticFolder(imageFile);

            if (existingStock.getGambarBarang() != null) {
                deleteOldImage(existingStock.getGambarBarang());
            }

            existingStock.setGambarBarang(newImagePath);
        }

        return stockRepository.save(existingStock);
    }

    public void deleteStock(Long id) {
        Stock stock = stockRepository.findById(id).orElseThrow(() -> new NotFoundException("Stock not found with id: " + id));
        stockRepository.delete(stock);
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

    private String saveImageToStaticFolder(MultipartFile imageFile) throws IOException {
        String fileName = UUID.randomUUID().toString() + "_" + imageFile.getOriginalFilename();
        String uploadDir = "src/main/resources/static/uploads/";
        Path uploadPath = Paths.get(uploadDir);

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

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
