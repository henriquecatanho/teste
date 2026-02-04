COPY operadoras(cnpj, razao_social, registro_ans, modalidade, uf)
FROM '/data/csvs/Relatorio_cadop.csv'
DELIMITER ';'
CSV HEADER
ENCODING 'UTF8';

COPY despesas_consolidado(cnpj, razao_social, ano, trimestre, valor)
FROM '/data/csvs/consolidado_sample.csv'
DELIMITER ';'
CSV HEADER
ENCODING 'UTF8';
