@echo off
echo ========================================
echo  COMPILANDO PROJETO ANS
echo ========================================
echo.

REM Criar diretórios necessários
if not exist "bin" mkdir bin
if not exist "bin\download" mkdir bin\download
if not exist "bin\processor" mkdir bin\processor
if not exist "dados_ans" mkdir dados_ans

REM Compilar downloaderAns.java
echo [1/2] Compilando downloaderAns.java...
javac -d bin -encoding UTF-8 download\downloaderAns.java
if %ERRORLEVEL% NEQ 0 (
    echo ERRO ao compilar downloaderAns.java!
    pause
    exit /b 1
)

REM Compilar AnsSimples.java
echo [2/2] Compilando AnsSimples.java...
javac -cp bin -d bin -encoding UTF-8 AnsSimples.java
if %ERRORLEVEL% NEQ 0 (
    echo ERRO ao compilar AnsSimples.java!
    pause
    exit /b 1
)

echo.
echo ========================================
echo  COMPILACAO CONCLUIDA COM SUCESSO!
echo ========================================
echo.
echo Para executar, use: executar.bat
echo.
pause