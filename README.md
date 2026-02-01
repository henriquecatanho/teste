# 🚀 ANS SIMPLES - SEM MAVEN!

## ✅ Versão Ultra Simplificada

Esta versão **NÃO PRECISA DE MAVEN**!
Apenas **Java puro** - sem dependências externas!

---

## 📋 Requisitos

- ✅ Java 17 ou superior
- ❌ Maven NÃO é necessário!
- ❌ Spring Boot NÃO é necessário!

---

## 🔧 Verificar se tem Java

Abra o CMD ou PowerShell e digite:

```bash
java -version
```

**Se aparecer algo como:**
```
java version "17.0.x" ou superior
```
✅ Está pronto para usar!

**Se aparecer erro:**
```
'java' nao e reconhecido...
```
❌ Precisa instalar Java: https://adoptium.net/

---

## 🚀 Como Usar (3 passos)

### 1️⃣ Compilar
```bash
compilar.bat
```

### 2️⃣ Executar
```bash
executar.bat
```

### 3️⃣ Ver resultados
```bash
# Arquivos baixados em:
downloads\

# Arquivos extraídos em:
extracted\
```

---

## 📁 Estrutura

```
ans-simples/
├── compilar.bat          ← Execute PRIMEIRO
├── executar.bat          ← Execute DEPOIS
├── src/
│   └── com/teste/ans/
│       └── AnsSimples.java
├── bin/                  (criado ao compilar)
├── downloads/            (criado ao executar)
└── extracted/            (criado ao executar)
```

---

## ⚡ Comandos Rápidos

```bash
# 1. Compilar
compilar.bat

# 2. Executar
executar.bat
```

Pronto! É só isso! 🎉

---

## 🎯 O que o programa faz

1. ✅ Acessa https://dadosabertos.ans.gov.br/FTP/PDA/
2. ✅ Lista os últimos 3 trimestres disponíveis
3. ✅ Para cada trimestre:
   - Encontra arquivos ZIP
  ````markdown
  # 🚀 ANS SIMPLES - INSTRUÇÕES PARA AVALIAÇÃO

  Este repositório contém as soluções do Teste ANS (consolidação, validação, enriquecimento e agregação). Abaixo estão instruções concisas para o avaliador — o objetivo é permitir executar a pipeline com amostra rápida ou com os dados completos.

  ---

  ## 📋 Requisitos

  - Java 17 ou superior
  - (Opcional) `curl` ou `wget` para baixar o cadastro ANS

  ---

  ## O que incluir no repositório (sugestão)

  - Código fonte Java: `*.java` (ex.: `AnsCorreto.java`, `EnriquecerOperadoras.java`, `Teste2Transformacao.java`, `Agrupador.java`, `UnmatchedResolver.java`, `ExtractInvalidCNPJ.java`)
  - Scripts úteis: `compilar.bat`, `executar.bat` (ou `build.sh`, `run.sh`)
  - `DOCUMENTACAO_SOLUCOES.md` (já incluído)
  - `README.md` (este arquivo)
  - `lib/` (opcional) com `opencsv`/`commons-lang3` ou instrução para baixar
  - `sample/` (pequena amostra CSV) para teste rápido — facilita validação sem dados grandes

  Arquivos que não devem ser versionados (adicionar em `.gitignore`):
  - `/bin/` or `*.class`
  - `consolidado_*.csv`, `invalid_*.csv`, `Teste_*.zip`, arquivos `.zip` grandes
  - `/downloads/`, `/extracted/`

  ---

  ## Passos rápidos para o avaliador (modo sample)

  1) Compilar todos os fontes:

  ```bash
  javac -cp "lib/*;." *.java
  ```

  2) Rodar um teste rápido com sample (se presente):

  ```bash
  # usa um CSV pequeno em sample/consolidado_sample.csv
  java Agrupador sample/consolidado_sample.csv Teste_Sample
  ```

  3) Resultados rápidos:

  - `Teste_Sample_despesas_agregadas.csv`

  ---

  ## Executando a pipeline completa (se quiser rodar com dados reais)

  1) Baixar/colocar `Relatorio_cadop.csv` em `dados_ans/` (ou usar o script fornecido):

  ```bash
  # exemplo para baixar (pode falhar dependendo de rede)
  curl -L -o dados_ans/Relatorio_cadop.csv "https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv"
  ```

  2) Rodar consolidator (gera `consolidado_despesas.csv`):

  ```bash
  java AnsCorreto
  ```

  3) Validar/transformar se desejar (opcional):

  ```bash
  java Teste2Transformacao consolidado_despesas.csv consolidado_validado.csv despesas_agregadas.csv
  ```

  4) Enriquecer com cadastro:

  ```bash
  java EnriquecerOperadoras consolidado_despesas.csv dados_ans/Relatorio_cadop.csv consolidado_enriquecido_out.csv despesas_agregadas_temp.csv
  ```

  5) Agregar e ordenar (gera `Teste_<seu_nome>_despesas_agregadas.csv`):

  ```bash
  java Agrupador consolidado_enriquecido_out.csv Teste_SeuNome
  ```

  6) (Opcional) Heurísticas / auditoria:

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

| Recurso | Versão Maven | Versão Simples |
|---------|--------------|----------------|
| Precisa Maven | ✅ Sim | ❌ Não |
| Precisa Spring Boot | ✅ Sim | ❌ Não |
| API REST | ✅ Sim | ❌ Não |
| Processa CSV/XLSX | ✅ Sim | ⚠️ Básico |
| Facilidade | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

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
