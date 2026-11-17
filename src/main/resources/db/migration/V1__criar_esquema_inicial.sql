CREATE TABLE tb_doca (
    id BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(255) NOT NULL UNIQUE,
    tipo VARCHAR(50) NOT NULL,
    ativa BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE tb_fornecedor (
    id BIGSERIAL PRIMARY KEY,
    cnpj VARCHAR(20) NOT NULL UNIQUE,
    razao_social VARCHAR(255) NOT NULL
);

CREATE TABLE tb_agendamento (
    id BIGSERIAL PRIMARY KEY,
    fornecedor_id BIGINT NOT NULL,
    doca_id BIGINT NOT NULL,
    placa_veiculo VARCHAR(20),
    inicio TIMESTAMP,
    fim TIMESTAMP,
    quantidade_paletes INTEGER NOT NULL,
    tipo_carga VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    motivo_cancelamento VARCHAR(255),
    criado_em TIMESTAMP,
    
    CONSTRAINT fk_agendamento_fornecedor 
        FOREIGN KEY (fornecedor_id) REFERENCES tb_fornecedor (id),
    CONSTRAINT fk_agendamento_doca 
        FOREIGN KEY (doca_id) REFERENCES tb_doca (id)
);