import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.zip.*;

public class AnsCorreto {
    
    private static final String INPUT_DIR = "dados_ans";
    private static final String OUTPUT_FILE = "consolidado_despesas.csv";
    
    private static final Set<String> CONTAS_DESPESAS = new HashSet<>(Arrays.asList(
        "41", "411", "4111", "41111",
        "412", "4121", "41211",
        "413", "4131",
        "44", "441", "4411"
    ));
    
    public static void main(String[] args) {
        try {
            System.out.println("Consolidação de dados de despesas");

            AnsCorreto consolidador = new AnsCorreto();

            System.out.println("Buscando arquivos CSV...");
            List<File> csvs = consolidador.buscarCSVs();
            
            if (csvs.isEmpty()) {
                System.out.println("Nenhum CSV encontrado.");
                return;
            }

            System.out.println("Encontrados " + csvs.size() + " arquivo(s):");
            for (File f : csvs) {
                System.out.println("  " + f.getName() + " (" + formatBytes(f.length()) + ")");
            }

            System.out.println("Processando dados...");
            List<RegistroDespesa> dados = consolidador.processarTodos(csvs);
            
            System.out.println("Total de registros extraídos: " + dados.size());

            System.out.println("Analisando inconsistências...");
            consolidador.analisarInconsistencias(dados);

            System.out.println("Salvando arquivo consolidado...");
            consolidador.salvarCSV(dados);

            System.out.println("Compactando arquivo...");
            consolidador.compactarArquivo();

            System.out.println("Consolidação concluída.");

            System.out.println("Arquivos gerados:");
            System.out.println("  " + OUTPUT_FILE);
            System.out.println("  consolidado_despesas.zip");
            
        } catch (Exception e) {
            System.err.println("ERRO: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    static class RegistroDespesa {
        String cnpj;
        String razaoSocial;
        String trimestre;
        String ano;
        double valorDespesas;
        boolean cnpjInvalido = false;
        boolean valorNegativo = false;
        boolean razaoSocialVazia = false;
        
        RegistroDespesa(String regAns, String descricao, String data, double valor) {
            this.cnpj = regAns;
            this.razaoSocial = descricao;
            this.valorDespesas = valor;
            if (data != null && data.length() >= 7) {
                String[] partes = data.split("-");
                this.ano = partes[0];
                int mes = Integer.parseInt(partes[1]);
                this.trimestre = "T" + ((mes - 1) / 3 + 1);
            }
            if (razaoSocial == null || razaoSocial.trim().isEmpty()) razaoSocialVazia = true;
            if (valor < 0) valorNegativo = true;
        }
        String toCSVLine() {
            return String.format("%s;%s;%s;%s;%.2f", cnpj, razaoSocial, trimestre, ano, valorDespesas);
        }
    }
    
    List<File> buscarCSVs() throws IOException {
        List<File> csvs = new ArrayList<>();
        Files.walk(Paths.get(INPUT_DIR))
            .filter(Files::isRegularFile)
            .filter(p -> p.toString().toLowerCase().endsWith(".csv"))
            .forEach(p -> csvs.add(p.toFile()));
        return csvs;
    }
    
    List<RegistroDespesa> processarTodos(List<File> csvs) throws IOException {
        List<RegistroDespesa> todos = new ArrayList<>();
        for (File csv : csvs) {
            System.out.println("  Processando " + csv.getName() + "...");
            List<RegistroDespesa> dados = processarCSV(csv);
            System.out.println("     " + dados.size() + " registros extraídos");
            todos.addAll(dados);
        }
        return todos;
    }
    
    List<RegistroDespesa> processarCSV(File arquivo) throws IOException {
        List<RegistroDespesa> dados = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(arquivo), StandardCharsets.UTF_8))) {
            String cabecalho = reader.readLine();
            if (cabecalho == null) return dados;
            String linha;
            while ((linha = reader.readLine()) != null) {
                try {
                    RegistroDespesa registro = parseLinha(linha);
                    if (registro != null) dados.add(registro);
                } catch (Exception e) {
                }
            }
        }
        return dados;
    }
    
    RegistroDespesa parseLinha(String linha) {
        String[] campos = linha.replace("\"", "").split(";");
        if (campos.length < 6) return null;
        String data = campos[0];
        String regAns = campos[1];
        String codConta = campos[2];
        String descricao = campos[3];
        double valorFinal = parseDouble(campos[5]);
        if (!isDespesa(codConta)) return null;
        if (valorFinal == 0) return null;
        return new RegistroDespesa(regAns, descricao, data, valorFinal);
    }
    
    boolean isDespesa(String codConta) {
        if (codConta == null || codConta.isEmpty()) return false;
        for (String prefixo : CONTAS_DESPESAS) if (codConta.startsWith(prefixo)) return true;
        return false;
    }
    
    double parseDouble(String valor) {
        try { return Double.parseDouble(valor.replace(",", ".")); } catch (Exception e) { return 0.0; }
    }
    
    void analisarInconsistencias(List<RegistroDespesa> dados) {
        int cnpjsInvalidos = 0, valoresNegativos = 0, razoesSociaisVazias = 0;
        Map<String, Set<String>> cnpjsComMultiplasRazoes = new HashMap<>();
        for (RegistroDespesa d : dados) {
            if (d.cnpjInvalido) cnpjsInvalidos++;
            if (d.valorNegativo) valoresNegativos++;
            if (d.razaoSocialVazia) razoesSociaisVazias++;
            cnpjsComMultiplasRazoes.computeIfAbsent(d.cnpj, k -> new HashSet<>()).add(d.razaoSocial);
        }
        int cnpjsDuplicados = (int) cnpjsComMultiplasRazoes.entrySet().stream().filter(e -> e.getValue().size() > 1).count();
        System.out.println("Estatísticas:");
        System.out.println("  Total de registros: " + dados.size());
        System.out.println("  Valores negativos: " + valoresNegativos);
        System.out.println("  Razões sociais vazias: " + razoesSociaisVazias);
        System.out.println("  REG_ANS com múltiplas descrições: " + cnpjsDuplicados);
    }
    
    void salvarCSV(List<RegistroDespesa> dados) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(OUTPUT_FILE), StandardCharsets.UTF_8))) {
            writer.write("CNPJ;RazaoSocial;Trimestre;Ano;ValorDespesas\n");
            for (RegistroDespesa d : dados) writer.write(d.toCSVLine() + "\n");
        }
        File arquivo = new File(OUTPUT_FILE);
        System.out.println("Arquivo salvo: " + OUTPUT_FILE + " (" + formatBytes(arquivo.length()) + ")");
    }
    
    void compactarArquivo() throws IOException {
        String zipFile = "consolidado_despesas.zip";
        try (FileOutputStream fos = new FileOutputStream(zipFile); ZipOutputStream zos = new ZipOutputStream(fos); FileInputStream fis = new FileInputStream(OUTPUT_FILE)) {
            ZipEntry entry = new ZipEntry(OUTPUT_FILE);
            zos.putNextEntry(entry);
            byte[] buffer = new byte[8192]; int len;
            while ((len = fis.read(buffer)) > 0) zos.write(buffer, 0, len);
            zos.closeEntry();
        }
        File arquivo = new File(zipFile);
        System.out.println("Arquivo compactado: " + zipFile + " (" + formatBytes(arquivo.length()) + ")");
    }
    
    static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        return String.format("%.1f MB", bytes / (1024.0 * 1024.0));
    }
}
