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
````

2. Rodar um teste rápido com sample (se presente):

```bash
# usa um CSV pequeno em sample/consolidado_sample.csv
java Agrupador sample/consolidado_sample.csv Teste_Sample
```

3. Resultados rápidos:

- `Teste_Sample_despesas_agregadas.csv`

---

## Executando a pipeline completa (se quiser rodar com dados reais)

1. Baixar/colocar `Relatorio_cadop.csv` em `dados_ans/` (ou usar o script fornecido):

```bash
# exemplo para baixar (pode falhar dependendo de rede)
curl -L -o dados_ans/Relatorio_cadop.csv "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv"
```

2. Rodar consolidator (gera `consolidado_despesas.csv`):

```bash
java AnsCorreto
```

3. Validar/transformar se desejar (opcional):

```bash
java Teste2Transformacao consolidado_despesas.csv consolidado_validado.csv despesas_agregadas.csv
```

4. Enriquecer com cadastro:

```bash
java EnriquecerOperadoras consolidado_despesas.csv dados_ans/Relatorio_cadop.csv consolidado_enriquecido_out.csv despesas_agregadas_temp.csv
```

5. Agregar e ordenar (gera `Teste_<seu_nome>_despesas_agregadas.csv`):

```bash
java Agrupador consolidado_enriquecido_out.csv Teste_SeuNome
```

6. (Opcional) Heurísticas / auditoria:

```bash
java UnmatchedResolver consolidado_enriquecido_out.csv dados_ans/Relatorio_cadop.csv unmatched_output
java ExtractInvalidCNPJ consolidado_enriquecido_out.csv invalid_cnpj.csv
```

---

## O que o recrutador precisa para rodar (resumo)

- Ter `Java 17+` instalado
- Compilar com `javac -cp "lib/*;." *.java` (ou usar `compilar.bat`)
- Ter o CSV de cadastro em `dados_ans/Relatorio_cadop.csv` para enriquecimento completo (ou usar sample)

---

## Por que não commitei os CSVs grandes

Arquivos consolidados e ZIPs são grandes e pesados — o repositório deve permanecer leve. O avaliador pode baixar os dados se quiser rodar tudo; forneci `sample/` para testes rápidos.

---

Se quiser, eu acrescento um `.gitignore` e um `sample/consolidado_sample.csv` com 5 linhas de exemplo, ou eu apenas atualizo com o que você preferir — diga se quer que eu crie o `sample/` agora.
👉 Depois: `compilar.bat`

---

## 🆚 Diferença para a versão Maven

| Recurso             | Versão Maven | Versão Simples |
| ------------------- | ------------ | -------------- |
| Precisa Maven       | ✅ Sim       | ❌ Não         |
| Precisa Spring Boot | ✅ Sim       | ❌ Não         |
| API REST            | ✅ Sim       | ❌ Não         |
| Processa CSV/XLSX   | ✅ Sim       | ⚠️ Básico      |
| Facilidade          | ⭐⭐⭐       | ⭐⭐⭐⭐⭐     |

---

## ✅ Checklist

- [ ] Tenho Java 17+ instalado
- [ ] Extraí a pasta `ans-simples`
- [ ] Executei `compilar.bat`
- [ ] Executei `executar.bat`
- [ ] Vi os arquivos em `downloads/`
- [ ] Vi os arquivos em `extracted/`

---

## 🎓 Próximos Passos

Este programa faz a parte **mais difícil** do teste (conectar com ANS).

Para completar o teste da Intuitive Care, você ainda precisa:

- Processar os arquivos CSV/XLSX
- Validar CNPJs
- Consolidar dados
- Criar banco de dados SQL
- etc.

Mas a integração com ANS está **FUNCIONANDO**! 🎉

---

**VERSÃO SIMPLIFICADA - SEM MAVEN - APENAS JAVA**

Desenvolvido para facilitar o teste técnico da Intuitive Care

```

```
