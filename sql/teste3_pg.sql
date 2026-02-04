-- teste3_pg.sql
-- DDL, transform and analytical queries for Teste 3 (PostgreSQL)
-- Edit paths below before running COPY/\copy commands

-- 1. DDL: operadoras, staging and final tables
CREATE TABLE IF NOT EXISTS operadoras (
  operadora_id BIGSERIAL PRIMARY KEY,
  registro_ans VARCHAR(32) UNIQUE,
  cnpj_digits VARCHAR(32) UNIQUE,
  razao_social TEXT,
  modalidade TEXT,
  uf CHAR(2),
  created_at TIMESTAMP DEFAULT now()
);

-- staging table for consolidado CSV import (all text to allow cleaning)
CREATE TABLE IF NOT EXISTS consolidado_tmp (
  linha_id BIGSERIAL PRIMARY KEY,
  razao_social TEXT,
  cnpj_raw TEXT,
  valor_raw TEXT,
  trimestre_raw TEXT,
  extras JSONB
);

-- final cleaned despesas
CREATE TABLE IF NOT EXISTS despesas_consolidado (
  despesa_id BIGSERIAL PRIMARY KEY,
  operadora_id BIGINT REFERENCES operadoras(operadora_id),
  registro_ans VARCHAR(32),
  cnpj_digits VARCHAR(32),
  razao_social TEXT,
  valor DECIMAL(18,2),
  year SMALLINT,
  quarter SMALLINT,
  quarter_start DATE,
  source_file TEXT,
  created_at TIMESTAMP DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_despesa_operadora_quarter ON despesas_consolidado(operadora_id, year, quarter);
CREATE INDEX IF NOT EXISTS idx_despesa_cnpj ON despesas_consolidado(cnpj_digits);

-- rejected rows for manual inspection
CREATE TABLE IF NOT EXISTS rejeitados_importacao (
  rej_id BIGSERIAL PRIMARY KEY,
  source_table TEXT,
  linha_raw JSONB,
  reason TEXT,
  created_at TIMESTAMP DEFAULT now()
);

-- helper function to strip non-digits
CREATE OR REPLACE FUNCTION digits_only(text) RETURNS text AS $$
  SELECT regexp_replace($1, '\D','','g');
$$ LANGUAGE SQL IMMUTABLE;

-- ====================================================================================
-- IMPORT EXAMPLES (run from psql client so \copy runs on client side)
-- Adjust file paths before running. Example (psql):
-- \c ans_test ans_user
-- \copy consolidado_tmp(razao_social,cnpj_raw,valor_raw,trimestre_raw) FROM 'C:/path/to/consolidado_despesas.csv' WITH (FORMAT csv, DELIMITER ';', HEADER true, ENCODING 'UTF8');

-- If the operadoras CSV has columns: RegistroANS;CNPJ;RazaoSocial;Modalidade;UF
-- create a temporary table matching its columns and import then upsert into `operadoras`.

-- Example for operadoras import (adjust columns if needed):
-- CREATE TEMP TABLE operadoras_tmp (registro_ans TEXT, cnpj_raw TEXT, razao_social TEXT, modalidade TEXT, uf TEXT);
-- \copy operadoras_tmp FROM 'C:/path/to/Relatorio_cadop.csv' WITH (FORMAT csv, DELIMITER ';', HEADER true, ENCODING 'UTF8');
-- INSERT INTO operadoras(registro_ans, cnpj_digits, razao_social, modalidade, uf)
-- SELECT registro_ans, digits_only(cnpj_raw), razao_social, modalidade, upper(uf)
-- FROM operadoras_tmp
-- ON CONFLICT (registro_ans) DO UPDATE SET razao_social = EXCLUDED.razao_social;

-- ====================================================================================
-- TRANSFORM: move valid rows from consolidado_tmp -> despesas_consolidado
-- Strategy: try to parse valor (replace comma by dot), parse trimestre like '2023T1'.

-- Insert valid rows
INSERT INTO despesas_consolidado (registro_ans, cnpj_digits, razao_social, valor, year, quarter, quarter_start, source_file)
SELECT
  NULLIF(trim(t.cnpj_raw),'') AS registro_ans_guess,
  digits_only(t.cnpj_raw) AS cnpj_digits,
  trim(t.razao_social) AS razao_social,
  CASE
    WHEN t.valor_raw IS NULL OR trim(t.valor_raw) = '' THEN NULL
    ELSE (replace(t.valor_raw,',','.') )::numeric
  END AS valor,
  CASE WHEN t.trimestre_raw ~ '^[0-9]{4}T[1-4]$' THEN (regexp_replace(t.trimestre_raw,'T.*','','g'))::int ELSE NULL END AS year,
  CASE WHEN t.trimestre_raw ~ '^[0-9]{4}T[1-4]$' THEN (regexp_replace(t.trimestre_raw,'.*T','','g'))::int ELSE NULL END AS quarter,
  CASE WHEN t.trimestre_raw ~ '^[0-9]{4}T[1-4]$' THEN make_date((regexp_replace(t.trimestre_raw,'T.*','','g'))::int, (((regexp_replace(t.trimestre_raw,'.*T','','g'))::int -1) * 3 + 1), 1) ELSE NULL END AS quarter_start,
  'consolidado_despesas.csv'
FROM consolidado_tmp t
WHERE (t.valor_raw IS NOT NULL AND trim(t.valor_raw) <> '' AND replace(t.valor_raw,',','.') ~ '^-?[0-9]+(\.[0-9]+)?$')
  AND (t.trimestre_raw IS NOT NULL AND t.trimestre_raw ~ '^[0-9]{4}T[1-4]$');

-- Insert failed rows to rejeitados_importacao for manual review
INSERT INTO rejeitados_importacao (source_table, linha_raw, reason)
SELECT 'consolidado_tmp', to_jsonb(t.*),
  CASE
    WHEN NOT (t.valor_raw IS NOT NULL AND trim(t.valor_raw) <> '' AND replace(t.valor_raw,',','.') ~ '^-?[0-9]+(\.[0-9]+)?$') THEN 'invalid_valor'
    WHEN NOT (t.trimestre_raw IS NOT NULL AND t.trimestre_raw ~ '^[0-9]{4}T[1-4]$') THEN 'invalid_trimestre'
    ELSE 'other'
  END
FROM consolidado_tmp t
WHERE NOT (
  (t.valor_raw IS NOT NULL AND trim(t.valor_raw) <> '' AND replace(t.valor_raw,',','.') ~ '^-?[0-9]+(\.[0-9]+)?$')
  AND (t.trimestre_raw IS NOT NULL AND t.trimestre_raw ~ '^[0-9]{4}T[1-4]$')
);

-- ====================================================================================
-- ANALYTICAL QUERIES

-- Query 1: top 5 operadoras by percent growth between first and last quarter
-- Note: requires operadoras table to map operadora_id (or join by cnpj_digits/registro_ans)
WITH qbounds AS (
  SELECT MIN((year::text||'-'||quarter::text)) AS first_q, MAX((year::text||'-'||quarter::text)) AS last_q FROM despesas_consolidado
), sums AS (
  SELECT COALESCE(o.operadora_id,0) AS operadora_id, COALESCE(o.razao_social, d.razao_social) AS razao_social,
    SUM(CASE WHEN (d.year::text||'-'||d.quarter::text) = (SELECT first_q FROM qbounds) THEN d.valor ELSE 0 END) AS valor_prim,
    SUM(CASE WHEN (d.year::text||'-'||d.quarter::text) = (SELECT last_q FROM qbounds) THEN d.valor ELSE 0 END) AS valor_ult
  FROM despesas_consolidado d
  LEFT JOIN operadoras o ON o.cnpj_digits = d.cnpj_digits OR o.registro_ans = d.registro_ans
  GROUP BY o.operadora_id, o.razao_social, d.razao_social
)
SELECT operadora_id, razao_social, valor_prim, valor_ult,
  CASE WHEN valor_prim IS NULL OR valor_prim = 0 THEN NULL ELSE ((valor_ult - valor_prim) / NULLIF(valor_prim,0)) * 100 END AS pct_growth
FROM sums
WHERE valor_prim IS NOT NULL AND valor_ult IS NOT NULL AND valor_prim > 0
ORDER BY pct_growth DESC
LIMIT 5;

-- Query 2: distribution by UF (top 5) and avg per operator
-- Requires UF to be present in `despesas_consolidado` or joined from `operadoras`.
-- Total by UF
SELECT COALESCE(o.uf, '??') AS uf, SUM(d.valor) AS total_uf
FROM despesas_consolidado d
LEFT JOIN operadoras o ON o.cnpj_digits = d.cnpj_digits OR o.registro_ans = d.registro_ans
GROUP BY COALESCE(o.uf,'??')
ORDER BY total_uf DESC
LIMIT 5;

-- Avg per operator in each UF
WITH per_op AS (
  SELECT COALESCE(o.uf,'??') AS uf, COALESCE(o.operadora_id,0) AS operadora_id, SUM(d.valor) AS op_total
  FROM despesas_consolidado d
  LEFT JOIN operadoras o ON o.cnpj_digits = d.cnpj_digits OR o.registro_ans = d.registro_ans
  GROUP BY COALESCE(o.uf,'??'), COALESCE(o.operadora_id,0)
)
SELECT uf, COUNT(*) AS operadoras_count, SUM(op_total) AS total_uf, AVG(op_total) AS avg_per_operadora
FROM per_op
GROUP BY uf
ORDER BY total_uf DESC
LIMIT 5;

-- Query 3: operadoras with expenses above the overall mean in at least 2 of 3 quarters (most recent 3 quarters)
WITH qs AS (
  SELECT year, quarter
  FROM despesas_consolidado
  GROUP BY year, quarter
  ORDER BY year, quarter
  DESC LIMIT 3
), op_quarter AS (
  SELECT COALESCE(o.operadora_id,0) AS operadora_id, d.year, d.quarter, SUM(d.valor) AS op_total
  FROM despesas_consolidado d
  LEFT JOIN operadoras o ON o.cnpj_digits = d.cnpj_digits OR o.registro_ans = d.registro_ans
  WHERE (d.year, d.quarter) IN (SELECT year, quarter FROM qs)
  GROUP BY COALESCE(o.operadora_id,0), d.year, d.quarter
), quarter_mean AS (
  SELECT year, quarter, AVG(op_total) AS mean_q FROM op_quarter GROUP BY year, quarter
), flagged AS (
  SELECT oq.operadora_id, oq.year, oq.quarter, CASE WHEN oq.op_total > qm.mean_q THEN 1 ELSE 0 END AS above_mean
  FROM op_quarter oq JOIN quarter_mean qm ON oq.year = qm.year AND oq.quarter = qm.quarter
)
SELECT COUNT(DISTINCT operadora_id) AS operadoras_with_at_least_2_above_mean
FROM (
  SELECT operadora_id, SUM(above_mean) AS cnt_above FROM flagged GROUP BY operadora_id HAVING SUM(above_mean) >= 2
) t;

-- end of file
