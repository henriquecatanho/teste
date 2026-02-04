CREATE TABLE operadoras (
  cnpj VARCHAR(14) PRIMARY KEY,
  razao_social TEXT NOT NULL,
  registro_ans VARCHAR(20),
  modalidade TEXT,
  uf CHAR(2)
);

CREATE TABLE despesas_consolidadas (
  id SERIAL PRIMARY KEY,
  cnpj VARCHAR(14) NOT NULL,
  razao_social TEXT,
  ano SMALLINT,
  trimestre SMALLINT,
  valor DECIMAL(18,2),
  FOREIGN KEY (cnpj) REFERENCES operadoras(cnpj)
);

CREATE INDEX idx_despesas_cnpj ON despesas_consolidadas(cnpj);
CREATE INDEX idx_despesas_ano_trimestre ON despesas_consolidadas(ano, trimestre);
