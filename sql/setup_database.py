import subprocess

def run(sql):
    subprocess.run(f"psql -U postgres -d ans_data -f {sql}", shell=True, check=True)

run('sql/01_create_tables.sql')
run('sql/02_import_data.sql')
run('sql/03_query_analysis.sql')
