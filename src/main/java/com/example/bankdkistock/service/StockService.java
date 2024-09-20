package com.example.bankdkistock.service;

import com.example.bankdkistock.dto.RequestStockDTO;
import com.example.bankdkistock.dto.ResponseStockDTO;
import com.example.bankdkistock.model.Stock;
import com.example.bankdkistock.model.User;
import com.example.bankdkistock.repository.StockRepository;
import com.example.bankdkistock.util.AuthenticatedUserUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.dao.DataIntegrityViolationException;
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
    private final ObjectMapper objectMapper;

    public StockService(
            StockRepository stockRepository,
            UserService userService,
            AuthenticatedUserUtil authenticatedUserUtil,
            ObjectMapper objectMapper
    ) {
        this.stockRepository = stockRepository;
        this.userService = userService;
        this.authenticatedUserUtil = authenticatedUserUtil;
        this.objectMapper = objectMapper;
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
                .map(stock -> {
                    try {
                        return convertToDTO(stock);
                    } catch (Exception e) {
                        throw new RuntimeException("Error converting stock to DTO", e);
                    }
                })
                .collect(Collectors.toList());
    }

    public ResponseStockDTO updateStock(Long id, RequestStockDTO requestStockDTO) throws Exception {
        String username = authenticatedUserUtil.getAuthenticatedUsername();
        User currentUser = userService.findByUsername(username);

        Optional<Stock> existingStockOptional = stockRepository.findById(id);
        if (existingStockOptional.isEmpty()) return null;

        Stock existingStock = existingStockOptional.get();
        updateStockDetails(existingStock, requestStockDTO, currentUser);

        Stock updatedStock = stockRepository.save(existingStock);
        return convertToDTO(updatedStock);
    }

    public ResponseStockDTO getStockById(Long id) {
        return stockRepository.findById(id)
                .map(stock -> {
                    try {
                        return convertToDTO(stock);
                    } catch (Exception e) {
                        throw new RuntimeException("Error converting stock to DTO", e);
                    }
                })
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

    private ResponseStockDTO convertToDTO(Stock stock) throws Exception {
        JsonNode additionalInfoJson = null;
        if (stock.getAdditionalInfo() != null) {
            additionalInfoJson = objectMapper.readTree(stock.getAdditionalInfo());
        }

        return ResponseStockDTO.builder()
                .id(stock.getId())
                .namaBarang(stock.getNamaBarang())
                .jumlahStok(stock.getJumlahStok())
                .nomorSeriBarang(stock.getNomorSeriBarang())
                .additionalInfo(additionalInfoJson)
                .gambarBarang(stock.getGambarBarang())
                .createdAt(stock.getCreatedAt())
                .createdBy(stock.getCreatedBy())
                .updatedAt(stock.getUpdatedAt())
                .updatedBy(stock.getUpdatedBy())
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

        byte[] fileBytes = imageFile.getBytes();

        if (!isValidImageSignature(fileBytes)) {
            throw new Exception("Invalid image file: file content does not match extension");
        }
    }

    private boolean isValidImageSignature(byte[] fileBytes) {
        // JPEG files start with FF D8 FF
        byte[] jpegSignature = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

        // PNG files start with 89 50 4E 47 0D 0A 1A 0A
        byte[] pngSignature = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

        if (fileBytes.length > 3 && startsWith(fileBytes, jpegSignature)) {
            return true;
        }

        return fileBytes.length > 8 && startsWith(fileBytes, pngSignature);
    }

    private boolean startsWith(byte[] fileBytes, byte[] signature) {
        for (int i = 0; i < signature.length; i++) {
            if (fileBytes[i] != signature[i]) {
                return false;
            }
        }
        return true;
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
