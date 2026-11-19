CREATE INDEX idx_agendamento_doca_inicio ON tb_agendamento (doca_id, inicio);

ALTER TABLE tb_agendamento
    ALTER COLUMN inicio SET NOT NULL,
    ALTER COLUMN fim SET NOT NULL,
    ALTER COLUMN placa_veiculo SET NOT NULL,
    ALTER COLUMN criado_em SET NOT NULL,
    ADD CONSTRAINT ck_agendamento_paletes CHECK (quantidade_paletes BETWEEN 1 AND 26);

WITH cnpjs AS (
    SELECT id, regexp_replace(cnpj, '[^0-9]', '', 'g') AS digitos,
           upper(regexp_replace(btrim(cnpj), '[./-]', '', 'g')) AS sem_mascara
    FROM tb_fornecedor
), normalizados AS (
    SELECT id, CASE WHEN length(digitos) = 14 THEN digitos ELSE sem_mascara END AS cnpj
    FROM cnpjs
)
UPDATE tb_fornecedor AS fornecedor
SET cnpj = normalizados.cnpj
FROM normalizados
WHERE fornecedor.id = normalizados.id AND fornecedor.cnpj <> normalizados.cnpj;

UPDATE tb_agendamento
SET placa_veiculo = upper(replace(btrim(placa_veiculo), '-', ''))
WHERE placa_veiculo <> upper(replace(btrim(placa_veiculo), '-', ''));
