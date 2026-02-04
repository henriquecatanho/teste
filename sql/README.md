SQL — Teste 3 (instruções práticas, iniciantes)

Este diretório reúne os scripts SQL e auxiliares usados para o Teste 3.

O que há aqui

- `01_create_tables.sql` / `01_create_tables_simple.sql` — DDL para criar as tabelas (versão simples disponível).
- `02_import_data_*.sql` — exemplos de comandos/import para Windows e container.
- `03_query_analysis.sql` / `03_query_analysis_simple.sql` — queries de análise (use a _simple_ se quiser começar rápido).
- `tools/` — scripts de verificação e utilitários (validação de CSV exportado, etc.).

Passos rápidos (modo iniciante, sem psql remoto)

1. Abra o DBeaver e conecte ao seu Postgres local (localhost:5432).
2. Execute `01_create_tables_simple.sql` no SQL Editor (selecionar tudo → Run/Ctrl+Enter).
3. Use o Import Data do DBeaver para carregar:
   - `csvs/Relatorio_cadop.csv` → tabelas `operadoras`
   - `csvs/consolidado_sample.csv` → tabela `consolidado_tmp`
     No import, use `;` como delimitador e confirme o preview antes de finalizar.
4. Rode os UPDATEs/INSERTs de transformação que alinham as colunas e populam `despesas` (veja o histórico de comandos no repositório — copie/cole no editor e execute).
5. Abra `03_query_analysis_simple.sql`, execute cada bloco (selecione + Ctrl+Enter) e exporte resultados pelo grid (Export Result → CSV → salvar em `sql/results`).

Dicas rápidas

- Normalizar CNPJ antes de comparar: `regexp_replace(cnpj,'[^0-9]','','g')`.
- Ao exportar CSV do DBeaver, escolha `UTF-8`, delimitador `;`, e marque `Include column headers`.
- Guarde os relatórios em `sql/results/` para manter o repositório organizado.

Problemas comuns e soluções

- Se o editor estiver em `N/A`, abra um novo SQL a partir da conexão (Database Navigator → conexão → SQL Editor → New SQL Script) e cole as queries.
- Se o DBeaver renomear colunas no import, copie os nomes do preview e rode um `UPDATE` para copiá-las para as colunas corretas antes do `INSERT` final.

Se quiser que eu gere comandos `psql \copy` para exportar relatórios automaticamente para `sql/results/`, eu escrevo e deixo prontos para você colar no terminal.

# Teste 3 - Banco e Análise

1. Criar banco:

```
createdb ans_data
```

2. Criar tabelas:

```
psql -U postgres -d ans_data -f sql/01_create_tables.sql
```

3. Copiar os CSVs para o servidor PostgreSQL e importar:

```
psql -U postgres -d ans_data -f sql/02_import_data_windows.sql    # use este em Windows
# OU (se usar Docker e montar o repo em /data):
psql -U postgres -d ans_data -f sql/02_import_data_container.sql
```

4. Rodar análises:

```
psql -U postgres -d ans_data -f sql/03_query_analysis.sql
```
