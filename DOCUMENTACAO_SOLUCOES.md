**Documentação de Interrupções e Soluções — Consolidador ANS**

Resumo: este documento lista os principais problemas (interrupções) encontrados durante a implementação do consolidador, a solução aplicada, a lógica por trás da decisão e trade-offs para tornar o processamento mais robusto e eficiente em produção.

- **Contexto do trabalho**: Identificação/baixada/extração de arquivos ZIP dos trimestres, parse e normalização de registros de despesas, consolidação, validação (CNPJ/valor/razão) e enriquecimento com cadastro de operadoras (RegistroANS, Modalidade, UF).

1. Problema: Trimestres/diretórios variáveis e files ZIP com estruturas incomuns
   - Sintoma: nem sempre os caminhos/nomes dos arquivos seguem padrão rígido (às vezes múltiplos arquivos por trimestre ou subpastas).
   - Solução aplicada: implementação resiliente no identificador de trimestres (checagem de URLs, aceitar múltiplos arquivos por trimestre, percorrer recursivamente ZIPs/entradas). Código: `AnsCorreto`.
   - Lógica/justificativa: tolerância a variações reduz risco de falha por pequenas mudanças de layout no servidor. Trade-off: aumento pequeno de complexidade/tempo de busca versus maior confiabilidade.

2. Problema: Arquivos ZIP com formatos variados (CSV, TXT, XLSX) e colunas em ordens diferentes
   - Sintoma: arquivos com colunas renomeadas, ordens distintas e formatos binários (XLSX).
   - Solução aplicada: foco inicial em CSVs; extrair todos os arquivos e identificar por heurística (nome/coluna). Adotei um parser robusto (OpenCSV) para CSVs; quando necessário, implementar leitor de XLSX (poi) seria próximo passo.
   - Lógica/justificativa: priorizar formato mais simples e dominante (CSV) permite progresso rápido. Trade-off: não tratar XLSX imediatamente reduz cobertura, mas evita dependências e complexidade até confirmar necessidade.

3. Problema: Campos CSV contendo `;` internos, aspas e quebras de linha (linhas multilinha)
   - Sintoma: split simples por `;` produzia linhas quebradas e colunas deslocadas; saídas com registros concatenados/incorretos.
   - Solução aplicada:
     - Implementação inicial: função `splitCsvLine` que reconhece aspas e preserva campos com `;` internos.
     - Evolução: migração para OpenCSV (`CSVReader`) com suporte nativo a campos entre aspas e quebras de linha internas.
   - Lógica/justificativa: usar biblioteca testada reduz riscos e código de manutenção; manual parsing só como fallback. Trade-off: adicionar JARs (`opencsv`, `commons-lang3`) e compilar com classpath, mas ganho de robustez justifica.

4. Problema: Formatação inconsistente de CNPJ / campo primeiro não era CNPJ
   - Sintoma: no `consolidado_despesas.csv` o primeiro campo às vezes continha `REG_ANS` (registro numérico) em vez de CNPJ, ou CNPJ sem formatação; muitas entradas marcadas inválidas.
   - Solução aplicada: durante o enriquecimento criei duas estratégias de join: (a) normalizar e buscar por CNPJ (apenas dígitos), (b) quando não houver match, tentar join por `REG_ANS` (registro da ANS) — observação: alguns arquivos tinham REG_ANS em vez de CNPJ.
   - Lógica/justificativa: isso recuperou a grande maioria dos matches. Trade-off: aceitar múltiplas chaves aumenta a chance de match correto, mas exige validação extra (possível conflito). Em conflitos, por ora escolhemos o primeiro registro e contamos como conflito (pode ser tratado por regra de versão/tempo posteriormente).

5. Problema: Valores negativos, zeros e inconsistências numéricas
   - Sintoma: muitos valores negativos ou zero detectados ao consolidar (59.506 negativos identificados).
   - Solução aplicada: validação em `Teste2Transformacao.java` que marca `ValidValor` se >0; registros com valores inválidos são gravados no `consolidado_validado.csv` com flags; a agregação ignora valores inválidos por padrão.
   - Lógica/justificativa: separar validação da correção automática permite auditoria humana; estratégia adotada: não corrigir valores sem regra clara, apenas marcar e excluir da agregação, evitando distorção de métricas.

6. Problema: Arquivos muito grandes (50–60MB cada), memória limitada
   - Sintoma: carregamento total em memória poderia causar OOM em máquinas com pouca RAM.
   - Solução aplicada: processamento incremental/streaming usando `BufferedReader` e escrita por streaming (`BufferedWriter`). Agregação feita em mapas que armazenam apenas chaves agregadas (`RazaoSocial+UF`) em memória — menor cardinalidade comparada ao total de linhas.
   - Lógica/justificativa: streaming possibilita processar arquivos grandes sem precisar de bancos. Trade-off: mapas de agregação ainda podem crescer; se o número de chaves for muito grande, recomendaria usar uma base externa (SQLite/Postgres) ou abordagem de map-reduce/arquivo particionado.

7. Problema: Dados duplicados com descrições distintas por mesmo `REG_ANS`
   - Sintoma: `REG_ANS` mapeado para múltiplas `RazaoSocial`/descrições (764 casos detectados).
   - Solução aplicada: contei e relatei (estatística). Para o join, quando há múltiplos cadastros para a mesma chave, registre como `conflict` e, por enquanto, adotei o primeiro registro como fonte para enriquecimento (loguei contagem de conflitos).
   - Lógica/justificativa: em ambiente real, regra de negócio deve definir prioridade (registro mais recente, maior cobertura geográfica, manual review). Aqui marquei e deixei opção para política futura.

8. Problema: Encoding e caracteres especiais (acentuação)
   - Sintoma: possíveis quebras de encoding/acentos estranhos.
   - Solução aplicada: garantir leitura/escrita com `Files.newBufferedReader`/`newBufferedWriter` usando padrão do sistema; se necessário, forçar `StandardCharsets.UTF_8` e tratar BOM.
   - Lógica/justificativa: assegurar UTF-8 em toda pipeline evita corrupção. Trade-off: pequenos ajustes em sistemas Windows onde locale difere.

9. Problema: Falta de logs estruturados e rastreabilidade
   - Solução aplicada: prints e mensagens resumidas no final (totais, matched/nomatch/conflicts). Recomendação: adicionar logging (SLF4J/Logback) com níveis e arquivos de auditoria para execuções de produção.

Comandos principais para reproduzir (diretório `C:/Users/henri/Downloads`):

```
javac -cp lib/* AnsCorreto.java
java -cp ".;lib/*" AnsCorreto            # gera consolidado_despesas.csv
javac Teste2Transformacao.java
java Teste2Transformacao consolidado_despesas.csv consolidado_validado.csv despesas_agregadas.csv
curl -O https://dadosabertos.ans.gov.br/FTP/PDA/operadoras_de_plano_de_saude_ativas/Relatorio_cadop.csv
javac EnriquecerOperadoras.java
java EnriquecerOperadoras consolidado_validado.csv Relatorio_cadop.csv consolidado_enriquecido.csv despesas_agregadas.csv Teste_HenriqueCatanho.zip
```

Recomendações futuras para robustez/eficiência (cenário real):

- Normalizar e validar CNPJ/REG_ANS no início da pipeline (remover espaços, padding, só dígitos).
- Adotar base de dados leve (SQLite/Postgres) para joins/aggregações quando a cardinalidade de chaves for alta.
- Implementar testes unitários para parsers e um pequeno conjunto de dados de integração com casos edge (quotes multilinha, campos com `;`).
- Adicionar monitoramento/telemetria (tempo de execução por arquivo, contagem de erros) e logging estruturado.
- Para conflitos de cadastro, adotar política (ex.: escolher registro mais recente, ou mesclar por regras) ou gerar arquivo para revisão manual.

10. Estratégia para CNPJs inválidos (escolha e implementação)
    - Problema: o arquivo consolidado contém CNPJs mal formatados, campos numéricos curtos (REG_ANS em vez de CNPJ) ou valores que não respeitam a máscara de 14 dígitos.
    - Escolha adotada: _manter os registros_, marcar/extraí-los para auditoria e não descartá-los automaticamente. Implementação prática:
      - Criei `ExtractInvalidCNPJ.java` que varre `consolidado_enriquecido_out.csv` e salva todas as linhas cujo campo `CNPJ` não valida como um CNPJ de 14 dígitos válidos (função `isValidCNPJ`). Resultado: `invalid_cnpj.csv`.
      - Para o join/enriquecimento: se o campo `CNPJ` não for válido, o pipeline já tenta fallback por `REG_ANS` (quando possível). Se não houver match, o registro permanece no consolidado final com `RegistroANS;Modalidade;UF` em branco.
    - Racional/Justificativa:
      - Preservar dados em vez de descartá-los evita perda de informação potencialmente recuperável por heurísticas ou revisão humana (ex.: corrigir formatação, identificar que o campo era REG_ANS).
      - A extração em arquivo separado permite revisão manual e aplicação de heurísticas off-line (por analista), mantendo a pipeline auditável.
    - Prós:
      - Não perde potencial correspondência; mantém trilha auditável.
      - Fácil de reprocessar após aplicar heurísticas ou correções manuais.
    - Contras:
      - Mantém dados possivelmente sujos na saída principal (a agregação pode incluir valores sem RegistroANS); porém agregações por `RazaoSocial+UF` continuam válidas quando `RazaoSocial` e `UF` estiverem presentes.
      - Requer esforço manual adicional para correção e revisão se muitas linhas estiverem inválidas.
    - Como rodar a extração de inválidos:
      - Compilar:
        ```bash
        javac ExtractInvalidCNPJ.java
        ```
      - Executar:
        ```bash
        java ExtractInvalidCNPJ consolidado_enriquecido_out.csv invalid_cnpj.csv
        ```
      - `invalid_cnpj.csv` será gerado para revisão humana.

    - Observação sobre trade-off técnico: automatizar correção (fuzzy, heurísticas de padding, trocar REG_ANS por CNPJ) pode recuperar registros, mas aumenta risco de false-positives. Preferi a abordagem conservadora (preservar + auditar) para garantir integridade dos dados agregados.

11. Heurísticas automáticas para registros sem match (implementação e resultados)
    - Objetivo: reduzir o número de registros no consolidado que não encontravam correspondência no cadastro de operadoras.
    - Estratégia aplicada (ordem executada):
      1.  Normalizar campo `CNPJ` removendo todos os caracteres não numéricos (apenas dígitos).
      2.  Se o campo contém entre 1 e 6 dígitos, tratá-lo como `REG_ANS` e procurar por `REGISTRO_OPERADORA` no cadastro.
      3.  Se o campo contém exatamente 14 dígitos, buscar por CNPJ no cadastro (match direto).
      4.  Se não houve match por CNPJ/REG_ANS, tentar `RazaoSocial` exata após normalização (remover pontuação, espaços extras e usar lower-case).
      5.  Se ainda sem match, executar correspondência fuzzy entre `RazaoSocial` do consolidado e das operadoras usando distância de Levenshtein; aceitar o melhor candidato quando a razão (distância / comprimento máximo) ≤ 0.25 (limiar conservador).
    - Implementação: classe `UnmatchedResolver.java` realiza o fluxo acima e produz dois artefatos:
      - `unmatched_output_corrected.csv` — versão do consolidado com colunas `RegistroANS;Modalidade;UF` preenchidas quando heurística encontrou correspondência.
      - `unmatched_output_heuristic_matches.csv` — relatório de auditoria por linha com a heurística usada e um `score` (0..1; para `byRegistro`/`byCNPJ`/`exactRazao` score=1.0, para `fuzzyRazao` score = razão normalizada).
    - Resultados obtidos na execução atual (dados de amostra):
      - Total de linhas processadas: 239,832
      - Resolvidos automaticamente: 238,478
      - Ainda não resolvidos: 1,354
      - Taxa de resolução: ~99.44%
    - Justificativa da escolha de heurísticas e limiar:
      - Uso de `REG_ANS` como fallback é necessário porque alguns arquivos de origem colocavam esse código no campo onde esperávamos CNPJ; é um join natural e de baixa ambiguidade quando presente.
      - Busca direta por CNPJ (14 dígitos) é determinística e preferida.
      - Correspondência exata de razão normalizada recupera casos em que formatação/acentuação diferem mas o texto base é idêntico.
      - Fuzzy (Levenshtein) com limiar 0.25 é uma escolha conservadora — reduz falsos positivos enquanto permite recuperar pequenas variações (typos, abreviações). O limiar foi escolhido empiricamente para este conjunto: pequeno o suficiente para evitar matches amplos, suficientemente alto para capturar variações menores.
    - Prós desta abordagem:
      - Alta taxa de recuperação automática (>=99% neste conjunto), reduzindo muito o trabalho manual.
      - Geração de arquivo de auditoria permite revisão humana apenas nos casos restantes e naquelas heurísticas menos confiáveis (fuzzy).
    - Contras / riscos:
      - Fuzzy matching ainda pode produzir false-positives; por isso todas as decisões automáticas são auditáveis e registradas no relatório.
      - Dependência da qualidade do campo `RazaoSocial` — quando as razões forem muito diferentes, heurísticas falham.
    - Recomendações operacionais:
      - Revisar `unmatched_output_heuristic_matches.csv` filtrando por `heuristic=fuzzyRazao` e `score` próximo ao limiar (ex.: >0.15) para validação manual.
      - Se for necessário um pipeline totalmente automático sem revisão humana, aumentar a robustez com regras adicionais (ex.: exigir UF igual, ou combinar múltiplas métricas de similaridade) e reduzir a aceitação de fuzzy.
    - Como rodar:
      - Compilar:
        ```bash
        javac UnmatchedResolver.java
        ```
      - Executar (exemplo usado neste workspace):
        ```bash
        java UnmatchedResolver consolidado_enriquecido_out.csv dados_ans/Relatorio_cadop.csv unmatched_output
        ```
      - Saídas: `unmatched_output_corrected.csv`, `unmatched_output_heuristic_matches.csv`.
