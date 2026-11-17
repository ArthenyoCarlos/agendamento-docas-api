
CREATE SEQUENCE seq_codigo_doca START WITH 1 INCREMENT BY 1;

ALTER TABLE tb_doca 
ALTER COLUMN codigo SET DEFAULT ('D-' || nextval('seq_codigo_doca'));