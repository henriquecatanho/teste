<#
 import_psql.ps1
 PowerShell helper to run psql \copy commands for the CSVs.

 Usage:
 .\import_psql.ps1 -Host localhost -Port 5432 -Db ans_test -User ans_user -CsvDir "C:\path\to\csvs"

#>
param(
  [string]$Host = 'localhost',
  [int]$Port = 5432,
  [string]$Db = 'ans_test',
  [string]$User = 'ans_user',
  [string]$CsvDir = '.'
)

function Run-Copy($table, $columns, $file) {
  $path = Join-Path $CsvDir $file
  Write-Host "Importing $file into $table..."
  $cmd = "\"\\copy $table($columns) FROM '$path' WITH (FORMAT csv, DELIMITER ';', HEADER true, ENCODING 'UTF8')\""
  & psql -h $Host -p $Port -U $User -d $Db -c $cmd
  if ($LASTEXITCODE -ne 0) { Write-Host "Import failed for $file" -ForegroundColor Red }
}

# ensure psql exists
if (-not (Get-Command psql -ErrorAction SilentlyContinue)) {
  Write-Host "psql not found in PATH. Install PostgreSQL client or run from psql shell." -ForegroundColor Yellow
  exit 1
}

# Import consolidado (uses sample file included)
Run-Copy 'consolidado_tmp' 'razao_social,cnpj_raw,valor_raw,trimestre_raw' 'consolidado_sample.csv'

# Import operadoras (file included in csvs/)
Run-Copy 'operadoras_tmp' 'registro_ans,cnpj_raw,razao_social,modalidade,uf' 'Relatorio_cadop.csv'

Write-Host "Imports finished. Now run the transformation queries in sql/teste3_pg.sql to populate final tables." -ForegroundColor Green
