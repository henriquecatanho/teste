# ANS Simples

## Resumo

Pequeno conjunto de utilitários Java para consolidar, validar, enriquecer e agregar dados de despesas (formato CSV). Projetado para ser fácil de rodar localmente com um conjunto de amostra.

## Pré-requisitos

- Java 17 ou superior instalado.
- (Opcional) `curl` ou `wget` para baixar o cadastro ANS se for rodar a pipeline completa.

## Testar rapidamente (modo sample)

1. Compile todos os fontes:

```bash
javac -cp "lib/*;." *.java
```

2. Rode um teste pequeno com a amostra:

```bash
java Agrupador sample/consolidado_sample.csv Teste_Sample
```

Saída esperada: `Teste_Sample_despesas_agregadas.csv` com a agregação da amostra.

## Pipeline completa (passos resumidos)

1. Baixe ou coloque `Relatorio_cadop.csv` em `dados_ans/` (opcional para enriquecimento):

```bash
curl -L -o dados_ans/Relatorio_cadop.csv "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv"
```

2. Rode o consolidador (gera `consolidado_despesas.csv`):

````bash
README — Teste 3 — Resumo humano e guia prático

Olá! Este repositório contém o material e os scripts desenvolvidos para o Teste 3. Vou ser direto e humano: expliquei os passos que demos, quais dificuldades apareciam e como você pode continuar com segurança.

Por que este README foi unificado
- Havia vários arquivos README com versões parcialmente duplicadas. Para facilitar a leitura e a avaliação, juntei as instruções essenciais e as decisões técnicas em um único documento claro e acessível.

O que fizemos aqui (visão resumida)
- Criamos um DDL simples (`sql/01_create_tables_simple.sql`) para facilitar a criação das tabelas `operadoras` e `despesas`.
- Carregamos os CSVs em uma tabela de staging (`consolidado_tmp`) usando o Import Data do DBeaver e aplicamos transformações SQL para normalizar os dados e popular `despesas`.
- Implementamos scripts de análise simples (`sql/03_query_analysis_simple.sql`) e ferramentas para validar exportações (`sql/tools/`).

Principais dificuldades enfrentadas (e o que aprendemos)
- Execução remota limitada: não pude executar `psql`/Docker neste ambiente, então foquei em entregar scripts e instruções reproduzíveis localmente.
- Importação GUI: o DBeaver às vezes altera nomes de colunas ao importar. Isso exigiu scripts adicionais para alinhar colunas antes do INSERT final.
- Formatação de números: valores com vírgula decimal precisaram de limpeza (trocar `,` por `.`) antes do cast para NUMERIC.

O que está pronto e testado
- DDL para criação de tabelas (arquivo em `sql/`).
- Scripts de transformação e INSERTs para popular `despesas` a partir do staging.
- Consultas de análise iniciais em `sql/03_query_analysis_simple.sql`.
- Scripts para validar CSVs exportados em `sql/tools/` (PowerShell e Bash/Python).

Como rodar (passos práticos e diretos)
1) Conectar ao banco no DBeaver (localhost:5432). Use a sua instância local PostgreSQL.
2) Criar as tabelas: abra `sql/01_create_tables_simple.sql` e execute.
3) Importe os CSVs com o Import Data do DBeaver (veja `sql/02_import_data_windows.sql` para referência de colunas). Mapear colunas conforme o preview.
4) Rodar as transformações: execute os UPDATEs/INSERTs sugeridos para alinhar colunas e popular `despesas`.
5) Rodar análises: abra `sql/03_query_analysis_simple.sql`, execute os blocos e use Export → CSV para salvar resultados em `sql/results/`.

Boas práticas rápidas
- Sempre normalize CNPJs antes de comparar: `regexp_replace(cnpj,'[^0-9]','','g')`.
- Use `UTF-8` e `;` ao exportar CSVs para compatibilidade com Excel/PT-BR.
- Organize saídas em `sql/results/` para evitar arquivos com nomes longos gerados automaticamente.

Se quiser que eu:
- gere os comandos `psql \copy` para exportar automaticamente os relatórios para `sql/results/`, eu escrevo e deixo prontos;
- renomeie este README para `README.md` no root (atualmente já atualizado aqui);
- produza um pequeno relatório final em CSV/JSON com os resultados das queries, me diga e eu preparo os comandos.

Obrigado pelo trabalho até aqui — os desafios foram reais, mas cada um deles deixou o fluxo mais robusto e você agora tem scripts e passos reproduzíveis para fechar o Teste 3.


---

## Passos rápidos para o avaliador (modo sample)

1. Compilar todos os fontes:

```bash
javac -cp "lib/*;." *.java
ANS Simples — Guia completo e didático

Visão geral
-----------
Projeto: utilitários Java para consolidar, validar, enriquecer e agregar dados de despesas em CSV (fontes ANS). Este documento explica passo a passo como reproduzir a pipeline localmente, descreve decisões técnicas, problemas enfrentados e soluções adotadas. É escrito para iniciantes e estudantes.

Por que Java (versão simples) e não Spring
-----------------------------------------
- Simplicidade: a solução visa ser fácil de compilar e executar sem dependências complexas. Usar classes Java simples evita a necessidade de configurar Maven/Gradle e reduz o atrito para avaliadores.
- Menos dependências: sem Spring não é preciso configurar injeção, contexto, servidor web ou empacotamento — apenas `javac` e `java`.
- Foco no problema: o objetivo é processar arquivos grandes e aplicar regras de transformação; isso é feito de forma direta com I/O stream e coleções Java, que são suficientes para um protótipo.

Estrutura do repositório
------------------------
- `*.java` — código-fonte: `AnsCorreto`, `Agrupador`, `EnriquecerOperadoras`, `UnmatchedResolver`, utilitários.
- `csvs/` — CSVs de teste/exemplo pequeno.
- `dados_ans/` — local sugerido para `Relatorio_cadop.csv` (cadastro de operadoras).
- `sql/` — scripts SQL: DDL, import helpers, transformações e queries analíticas.
- `sql/tools/` — scripts auxiliares para validar/checar CSVs exportados.
- `sample/` — amostras pequenas para testes rápidos.

Pré-requisitos
--------------
- Java 17 ou superior.
- (Opcional) PostgreSQL local para executar os scripts SQL.
- DBeaver (recomendado) para importar CSVs via GUI e exportar resultados.

Teste rápido com a amostra
--------------------------
1. Compilar os fontes:

```bash
javac -cp "lib/*;." *.java
```

2. Rodar o consolidador com o sample (gera `consolidado_despesas.csv`):

```bash
java AnsCorreto
```

Resultado: arquivo `consolidado_despesas.csv` no diretório do projeto.

Pipeline completa (passo a passo)
--------------------------------
1) Preparar o ambiente PostgreSQL

- Criar banco (exemplo):

```bash
createdb ans_teste
```

- Conectar e executar o DDL (cria `operadoras` e `despesas`):

```bash
psql -d ans_teste -f sql/01_create_tables.sql
```

2) Importar CSVs com DBeaver (GUI) — passo a passo

- Abra DBeaver e conecte ao seu banco PostgreSQL.
- Clique com o direito na conexão → Tools → Data Transfer → Import Data (ou use o menu de importação de tabelas).
- Selecione o arquivo CSV (`csvs/consolidado_sample.csv`) e escolha criar/usar a tabela `consolidado_tmp` como staging.
- Mapeie colunas: verifique nomes, tipos e encoding (`UTF-8`). Atenção: DBeaver pode renomear colunas; mantenha as colunas `_raw` intactas para debug.
- Repita para `csvs/Relatorio_cadop.csv` → tabela `operadoras`.

3) Normalização e carga para `despesas`

- Principais operações necessárias (exemplos):
	- Normalizar CNPJ: `regexp_replace(cnpj, '[^0-9]', '', 'g')`.
	- Converter valores: `replace(valor_raw, ',', '.')::numeric`.
	- Filtrar contas de despesa por prefixo (ex.: `cod_conta LIKE '41%'`).

- Execute os blocos SQL de transformação em `sql/02_import_data.sql` ou em `sql/teste3_pg.sql`.

Detalhes técnicos importantes
----------------------------
- Encoding: use `UTF-8` ao importar/exportar para evitar problemas com acentos.
- Delimitador: preferir `;` para compatibilidade com formatos PT-BR.
- Campos CSV com `;` internos: use leitor robusto (OpenCSV em Java) ou o importador do DBeaver que respeita aspas.
- Linhas com valores inválidos (ex.: valor não numérico, CNPJ mal formatado) devem ser preservadas em uma tabela ou arquivo de auditoria para revisão manual.

Problemas comuns e como resolvê-los
----------------------------------
1) ERRO de FK ao inserir em `despesas` — motivo: CNPJs em staging não existem em `operadoras`.
	 Soluções:
	 - Inserir placeholders mínimos em `operadoras` para os CNPJs faltantes (id + nome genérico).
	 - Alterar o INSERT para `despesas` usando `JOIN` com `operadoras` (insere apenas matches).
	 - Remover temporariamente a constraint FK, inserir, depois corrigir e recriar a constraint (não recomendado sem validação).

2) Colunas renomeadas no import do DBeaver
	 - Verifique o mapeamento no preview e ajuste os nomes antes do commit do import.
	 - Se já importado, use UPDATEs para copiar do `_raw` para as colunas esperadas.

3) Valores numéricos com vírgula
	 - Antes do cast para numeric, aplique `replace(valor_raw, ',', '.')`.

Comandos úteis `psql` para exportar resultados (exemplos)
----------------------------------------------------
- Exportar top 10 operadoras por total para CSV:

```bash
psql -d ans_teste -U postgres -c "\copy (SELECT razao_social, SUM(valor) AS total FROM despesas GROUP BY razao_social ORDER BY total DESC LIMIT 10) TO 'sql/results/top10.csv' WITH CSV DELIMITER ';' HEADER ENCODING 'UTF8'"
```

- Exportar por ano/trimestre:

```bash
psql -d ans_teste -U postgres -c "\copy (SELECT ano, trimestre, SUM(valor) FROM despesas GROUP BY ano, trimestre ORDER BY ano, trimestre) TO 'sql/results/por_trimestre.csv' WITH CSV DELIMITER ';' HEADER ENCODING 'UTF8'"
```

Validação dos CSVs exportados
-----------------------------
- Use os scripts em `sql/tools/` para verificar delimitador, encoding, número de colunas e somas básicas.
- No Windows é comum usar PowerShell para abrir/inspecionar: `Import-Csv -Path sql/results/top10.csv -Delimiter ';' | Measure-Object -Property total -Sum`.

Exemplos de transformações SQL (padrões)
---------------------------------------
- Normalizar CNPJ:

```sql
UPDATE consolidado_tmp
SET cnpj_norm = regexp_replace(cnpj_raw, '[^0-9]', '', 'g');
```

- Converter valor para numeric:

```sql
UPDATE consolidado_tmp
SET valor_num = replace(valor_raw, ',', '.')::numeric
WHERE valor_raw ~ '^[0-9.,]+';
```

Checklist de execução (passo a passo)
-----------------------------------
1. Compilar Java: `javac -cp "lib/*;." *.java`
2. (Opcional) Preparar DB: `psql -d ans_teste -f sql/01_create_tables.sql`
3. Importar CSVs via DBeaver para `consolidado_tmp` e `operadoras`.
4. Executar transformações SQL em `sql/02_import_data.sql`.
5. Popular `despesas` a partir de staging (INSERTs filtrados/normalizados).
6. Executar queries analíticas em `sql/03_query_analysis.sql`.
7. Exportar resultados para `sql/results/` com `psql \copy` ou export do DBeaver.
8. Rodar scripts de verificação em `sql/tools/`.

Boas práticas e recomendações finais
-----------------------------------
- Sempre mantenha os arquivos de raw e `_raw` para auditar transformações.
- Não recrie constraints até validar os dados importados; prefira criar índices apenas após validação.
- Documente heurísticas de match (REG_ANS vs CNPJ vs RazaoSocial) em arquivos de auditoria.

Onde editar/ajustar
-------------------
- `sql/01_create_tables.sql` — ajustar DDL se desejar campos adicionais.
- `sql/02_import_data.sql` e `sql/teste3_pg.sql` — blocos de normalização e INSERTs.
- `sql/03_query_analysis.sql` — queries finais para export.

Suporte e próximos passos
-------------------------
Se desejar, eu posso:
- Gerar comandos `psql \copy` para cada relatório e deixar prontos.
- Criar um `README_SUMMARY.md` mais curto para avaliadores.
- Criar um script para automatizar export para `sql/results/`.

Fim.

---

Arquivos removidos: `README_UNIFIED.md`, `DOCUMENTACAO_SOLUCOES.md`, `sql/README.md`, `sql/README_test3.md` (foram consolidados aqui).

Obrigado — se quiser que eu também crie um changelog ou tag de release, eu faço em seguida.
````
