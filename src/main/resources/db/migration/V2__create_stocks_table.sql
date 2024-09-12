CREATE SEQUENCE IF NOT EXISTS stock_seq START WITH 1 INCREMENT BY 1;

CREATE TABLE stocks
(
    id                BIGINT DEFAULT nextval('stock_seq') NOT NULL,
    nama_barang       VARCHAR(255)                NOT NULL,
    jumlah_stok       INTEGER                     NOT NULL,
    nomor_seri_barang VARCHAR(255)                NOT NULL,
    additional_info   JSONB,
    gambar_barang     VARCHAR(255),
    created_at        TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    created_by        BIGINT                      NOT NULL,
    updated_at        TIMESTAMP WITHOUT TIME ZONE,
    updated_by        BIGINT,
    CONSTRAINT pk_stock PRIMARY KEY (id)
);

ALTER TABLE stocks
    ADD CONSTRAINT uc_stock_nomorseribarang UNIQUE (nomor_seri_barang);
