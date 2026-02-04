WITH primeiro AS (
  SELECT cnpj, MIN(ano*10+trimestre) AS periodo, SUM(valor) AS valor_inicial
  FROM despesas_consolidadas
  GROUP BY cnpj
),
ultimo AS (
  SELECT cnpj, MAX(ano*10+trimestre) AS periodo, SUM(valor) AS valor_final
  FROM despesas_consolidadas
  GROUP BY cnpj
)
SELECT o.razao_social, o.cnpj, p.valor_inicial, u.valor_final,
  ((u.valor_final - p.valor_inicial) / NULLIF(p.valor_inicial,0) * 100) AS crescimento_percentual
FROM primeiro p
JOIN ultimo u ON p.cnpj = u.cnpj
JOIN operadoras o ON o.cnpj = p.cnpj
WHERE p.valor_inicial > 0
ORDER BY crescimento_percentual DESC
LIMIT 5;

SELECT COALESCE(o.uf,'??') AS uf, SUM(d.valor) AS total_uf
FROM despesas_consolidadas d
LEFT JOIN operadoras o ON o.cnpj = d.cnpj
GROUP BY COALESCE(o.uf,'??')
ORDER BY total_uf DESC
LIMIT 5;
