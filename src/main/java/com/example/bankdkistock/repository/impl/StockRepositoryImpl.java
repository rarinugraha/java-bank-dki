package com.example.bankdkistock.repository.impl;

import com.example.bankdkistock.model.Stock;
import com.example.bankdkistock.repository.StockRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public class StockRepositoryImpl implements StockRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public Stock save(Stock stock) {
        if (stock.getId() == null) {
            entityManager.createNativeQuery(
                    "INSERT INTO stocks (nama_barang, jumlah_stok, nomor_seri_barang, additional_info, gambar_barang, created_at, created_by) " +
                                    "VALUES (:namaBarang, :jumlahStok, :nomorSeriBarang, cast(:additionalInfo as jsonb), :gambarBarang, :createdAt, :createdBy)")
                    .setParameter("namaBarang", stock.getNamaBarang())
                    .setParameter("jumlahStok", stock.getJumlahStok())
                    .setParameter("nomorSeriBarang", stock.getNomorSeriBarang())
                    .setParameter("additionalInfo", stock.getAdditionalInfo())
                    .setParameter("gambarBarang", stock.getGambarBarang())
                    .setParameter("createdAt", stock.getCreatedAt())
                    .setParameter("createdBy", stock.getCreatedBy())
                    .executeUpdate();
        } else {
            entityManager.createNativeQuery(
                    "UPDATE stocks SET nama_barang = :namaBarang, jumlah_stok = :jumlahStok, nomor_seri_barang = :nomorSeriBarang, " +
                                    "additional_info = cast(:additionalInfo as jsonb), gambar_barang = :gambarBarang, updated_at = :updatedAt, updated_by = :updatedBy " +
                                    "WHERE id = :id")
                    .setParameter("namaBarang", stock.getNamaBarang())
                    .setParameter("jumlahStok", stock.getJumlahStok())
                    .setParameter("nomorSeriBarang", stock.getNomorSeriBarang())
                    .setParameter("additionalInfo", stock.getAdditionalInfo())
                    .setParameter("gambarBarang", stock.getGambarBarang())
                    .setParameter("updatedAt", stock.getUpdatedAt())
                    .setParameter("updatedBy", stock.getUpdatedBy())
                    .setParameter("id", stock.getId())
                    .executeUpdate();
        }

        return stock;
    }

    @Override
    public Optional<Stock> findById(Long id) {
        try {
            String sql = "SELECT id, nama_barang, jumlah_stok, nomor_seri_barang, " +
                    "cast(additional_info as jsonb) as additional_info, gambar_barang, created_at, created_by, updated_at, updated_by " +
                    "FROM stocks WHERE id = :id";

            Object[] result = (Object[]) entityManager.createNativeQuery(sql)
                    .setParameter("id", id)
                    .getSingleResult();

            Stock stock = Stock.builder()
                    .id(((Number) result[0]).longValue())
                    .namaBarang((String) result[1])
                    .jumlahStok(((Number) result[2]).intValue())
                    .nomorSeriBarang((String) result[3])
                    .additionalInfo((String) result[4])
                    .gambarBarang((String) result[5])
                    .createdAt(((Timestamp) result[6]).toLocalDateTime())
                    .createdBy((Long) result[7])
                    .updatedAt(result[8] != null ? ((Timestamp) result[8]).toLocalDateTime() : null)
                    .updatedBy((Long) result[9])
                    .build();

            return Optional.of(stock);
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Stock> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Stock> query = cb.createQuery(Stock.class);
        Root<Stock> stock = query.from(Stock.class);
        query.select(stock);

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public void delete(Stock stock) {
        entityManager.remove(entityManager.contains(stock) ? stock : entityManager.merge(stock));
    }
}