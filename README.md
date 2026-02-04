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
**ANS Simples — Teste 3 (unificado)**

Resumo
-------
Conjunto de utilitários Java e scripts SQL para consolidar, validar, enriquecer e agregar dados de despesas (CSV) obtidos da ANS. Este README unifica a documentação do projeto e descreve passos reproduzíveis para rodar a análise localmente.

Estrutura do repositório
-------------------------
- `*.java` : fontes Java (consolidador, transformações, enriquecimento, utilitários)
- `csvs/` : CSVs de entrada de exemplo (pequenos)
- `dados_ans/` : local esperado para `Relatorio_cadop.csv` (cadastro de operadoras)
- `sql/` : DDL, scripts de importação, queries de análise e ferramentas auxiliares
- `sql/tools/` : scripts para validação de export (PowerShell / Bash / Python)
- `sample/` : exemplos pequenos para testes rápidos

Pré-requisitos
--------------
- Java 17+ instalado
- (Opcional) PostgreSQL local se quiser usar os scripts `sql/*.sql` com `psql`
- DBeaver recomendado para import via GUI (Import Data)

Rápido — testar com o sample
---------------------------
1. Compilar fontes:

```bash
javac -cp "lib/*;." *.java
```

2. Rodar o consolidador com o sample (gera `consolidado_despesas.csv`):

```bash
java AnsCorreto
```

Passos completos (pipeline local reproducível)
--------------------------------------------
1) Preparar banco (opcional, se usar SQL)

- Em PostgreSQL execute `sql/01_create_tables.sql` para criar `operadoras` e `despesas`.

2) Importar CSVs (DBeaver recomendado)

- Use Import Data do DBeaver para carregar:
	- `csvs/consolidado_sample.csv` → tabela de staging `consolidado_tmp`
	- `csvs/Relatorio_cadop.csv` → tabela `operadoras` (ou `dados_ans/Relatorio_cadop.csv` via script)
- Atenção ao mapear colunas — DBeaver pode renomear colunas automaticamente; confira as colunas `_raw` e ajuste antes de inserir.

3) Normalizar e popular `despesas`

- Execute os UPDATEs e INSERTs em `sql/02_import_data.sql` (ou use os blocos em `sql/teste3_pg.sql`) para:
	- normalizar `CNPJ` (remover não-dígitos),
	- converter `valor` (`replace(',', '.')` → cast numeric),
	- filtrar contas de despesas (prefixos) e inserir em `despesas`.

4) Análises

- Abra `sql/03_query_analysis.sql` ou `sql/03_query_analysis_simple.sql` e execute os blocos de consulta.
- Exporte resultados via DBeaver (Export Result → CSV) para `sql/results/`.

5) Validar exportações

- Use os scripts em `sql/tools/` para checar formato e consistência dos CSVs exportados.

Principais decisões e problemas conhecidos
----------------------------------------
- Falta de `psql`/Docker no ambiente do assistente: os scripts foram preparados para execução local.
- Import via GUI pode requerer mapeamento manual de colunas.
- CNPJs e campos numéricos precisaram de normalização; o pipeline preserva linhas inválidas em arquivos de auditoria para revisão manual.

Onde estão os scripts importantes
--------------------------------
- `sql/01_create_tables.sql` — DDL mínimo
- `sql/02_import_data*.sql` — exemplos e helpers para importar/transformar
- `sql/teste3_pg.sql` — script completo com seções de transformação
- `sql/03_query_analysis*.sql` — queries analíticas (top, por trimestre, por UF)
- `sql/tools/` — validadores de export (PowerShell/Bash/Python)

Limpeza de docs
---------------
Removi READMEs redundantes e consolidei a documentação principal neste `README.md`. Se quiser uma versão mais curta para avaliadores, eu gero uma `README_SUMMARY.md`.

Commits e push
--------------
Fiz alterações locais nesta branch; se quiser que eu dê push, confirme e eu realizo o commit + push (ou já posso empurrar as mudanças se preferir que eu use as credenciais locais).

Próximos passos sugeridos
-------------------------
- Executar as queries analíticas e exportar CSVs para `sql/results/`.
- Revisar placeholders em `operadoras` e substituir por nomes reais quando possível.
- Opcional: gerar `psql \copy` prontos para exportar relatórios automaticamente.

Contato/ajuda
------------
Se quiser que eu:
- gere os comandos `psql \copy` para exportar automaticamente os relatórios para `sql/results/`, eu escrevo e deixo prontos;
- rode um push das mudanças (commit + push), diga e eu executo;
- crie um relatório final em CSV/JSON com os resultados das queries, eu preparo os comandos.

---

Arquivos removidos: `README_UNIFIED.md`, `DOCUMENTACAO_SOLUCOES.md`, `sql/README.md`, `sql/README_test3.md` (foram consolidados aqui).

Obrigado — se quiser que eu também crie um changelog ou tag de release, eu faço em seguida.
