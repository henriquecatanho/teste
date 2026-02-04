README - Teste 3 (unificado)

Resumo humano e instruções (versão iniciantes)

Obrigado por acompanhar este repositório — aqui está um resumo claro e direto do que fizemos, as dificuldades que enfrentamos e como continuar.

O que este projeto contém

- DDL mínimo para criar as tabelas usadas no teste: `sql/01_create_tables_simple.sql`.
- Scripts de importação e transformação usados para carregar os CSVs e preparar a tabela `despesas`.
- Consultas de análise simples em `sql/03_query_analysis_simple.sql`.
- Ferramentas para verificar e validar arquivos exportados em `sql/tools/`.

O que fizemos (passo a passo, com linguagem clara)

1. Preparação do banco

- Criamos as tabelas `operadoras` e `despesas` com um DDL simples para facilitar o entendimento e importação via DBeaver.

2. Importação dos CSVs

- Usamos o Import Data do DBeaver para carregar `Relatorio_cadop.csv` em `operadoras` e `consolidado_sample.csv` em uma tabela de staging (`consolidado_tmp`).
- No processo de import, nomes de colunas e formatos (ponto/virgula decimais) podem variar — isso exigiu mapeamento manual no cliente.

3. Transformação e normalização

- Ajustamos os dados importados com SQL (UPDATEs para alinhar colunas, INSERTs limpos para `despesas`) e tratamos CNPJs removendo caracteres não-numéricos.
- Havia linhas sem CNPJ correspondente em `operadoras`; optamos por duas estratégias viáveis: criar placeholders mínimos em `operadoras` ou inserir em `despesas` apenas quando houver correspondência. Ambas opções foram documentadas.

Principais dificuldades encontradas

- Ambiente do assistente: não foi possível executar `psql`/Docker diretamente daqui — fizemos a maior parte do trabalho via instruções e arquivos prontos para você executar localmente.
- Importação via GUI (DBeaver) frequentemente altera nomes de colunas; isso exigiu scripts de alinhamento (UPDATE) antes do INSERT final.
- Formatos de número com vírgula decimal exigiram limpeza (replace ',' -> '.') antes do cast para NUMERIC.

O que está pronto / testado

- Estrutura de tabelas criada (DDL mínimo).
- CSVs importados em staging e, após alinhamento, dados inseridos em `despesas` (exemplo de validação mostrou 10 linhas importadas no teste de amostra).
- Scripts de análise simples disponíveis em `sql/03_query_analysis_simple.sql`.
- Ferramentas de verificação de export criadas em `sql/tools/` (PowerShell e Bash/Python) para validar CSVs exportados.

Como executar as análises (passos simples)

1. Abra o DBeaver e conecte ao banco (localhost:5432, DB `ans_teste`, user `postgres`, senha que você definiu).
2. Abra `sql/03_query_analysis_simple.sql` no SQL Editor.
3. Execute cada bloco (selecione o bloco e pressione `Ctrl+Enter`).
4. Exporte resultados do grid: clique direito no resultado → Export Result → CSV → escolha pasta `sql/results`.

Boas práticas e recomendações

- Mantenha uma pasta `sql/results/` para todos os CSVs gerados (assim evitamos nomes longos criados pelo wizard do DBeaver).
- Quando exportar, use `UTF-8` e `;` como delimitador para compatibilidade com Excel/PT-BR.
- Verifique CNPJs: sempre normalize com `regexp_replace(cnpj,'[^0-9]','','g')` antes de comparar/associar chaves.

Próximos passos sugeridos (curto prazo)

- Rodar as queries analíticas e exportar os CSVs finais para `sql/results/`.
- Revisar os placeholders em `operadoras` e, se possível, substituir por nomes reais quando disponíveis.
- Opcional: recriar constraints e índices só após validação completa dos dados.

Se precisar que eu gere os CSVs das análises para você, me diga e eu preparo os comandos `psql \copy` prontos para executar no seu terminal.

---

Observação: apaguei READMEs redundantes e unifiquei a documentação principal neste arquivo (README_UNIFIED.md). Se preferir, eu renomeio para `README.md`.
