COPY operadoras(cnpj, razao_social, registro_ans, modalidade, uf)
FROM '/caminho/para/operadoras_ativas.csv'
DELIMITER ','
CSV HEADER
ENCODING 'UTF8';

COPY despesas_consolidadas(cnpj, razao_social, ano, trimestre, valor)
FROM '/caminho/para/consolidado_despesas.csv'
DELIMITER ','
CSV HEADER
ENCODING 'UTF8';
