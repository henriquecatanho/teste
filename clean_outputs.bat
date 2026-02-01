@echo off
echo Removendo arquivos de saída gerados (executar no diretório do projeto)...
del /f /q consolidado_enriquecido_out.csv 2>nul
del /f /q despesas_agregadas_temp.csv 2>nul
del /f /q invalid_cnpj.csv 2>nul
del /f /q unmatched_output_corrected.csv 2>nul
del /f /q unmatched_output_heuristic_matches.csv 2>nul
del /f /q Teste_HenriqueCatanho_despesas_agregadas.csv 2>nul
del /f /q Teste_HenriqueCatanho.zip 2>nul
rd /s /q bin 2>nul
rd /s /q out 2>nul
echo Limpeza finalizada.
pause
