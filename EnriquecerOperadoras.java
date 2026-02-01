import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EnriquecerOperadoras {

    static class Op { String registro, cnpj, razao, modalidade, uf; }

    public static void main(String[] args) throws Exception {
        if (args.length < 4) {
            System.err.println("Usage: java EnriquecerOperadoras <consolidado.csv> <Relatorio_cadop.csv> <out_enriquecido.csv> <out_agregadas.csv>");
            return;
        }
        Path consolidado = Paths.get(args[0]);
        Path relatorio = Paths.get(args[1]);
        Path outEnriq = Paths.get(args[2]);
        Path outAg = Paths.get(args[3]);

        System.out.println("Loading operadoras from: " + relatorio);
        Map<String, Op> byCnpj = new HashMap<>();
        Map<String, Op> byRegistro = new HashMap<>();
        try (BufferedReader br = Files.newBufferedReader(relatorio)) {
            String header = br.readLine();
            if (header == null) throw new IOException("Empty relatorio file");
            String[] hdr = header.split(";", -1);
            int regI = idx(hdr, "REGISTRO_OPERADORA");
            int cnpjI = idx(hdr, "CNPJ");
            int razI = idx(hdr, "Razao_Social");
            int modI = idx(hdr, "Modalidade");
            int ufI = idx(hdr, "UF");
            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = splitPreserve(line);
                Op o = new Op();
                o.registro = safe(cols, regI);
                o.cnpj = onlyDigits(safe(cols, cnpjI));
                o.razao = safe(cols, razI);
                o.modalidade = safe(cols, modI);
                o.uf = safe(cols, ufI);
                if (o.cnpj != null && o.cnpj.length()==14) byCnpj.put(o.cnpj, o);
                if (o.registro != null && !o.registro.isEmpty()) byRegistro.put(o.registro, o);
            }
        }

        System.out.println("Operators loaded: cnpj=" + byCnpj.size() + " registro=" + byRegistro.size());

        // read consolidado and enrich
        try (BufferedReader br = Files.newBufferedReader(consolidado);
             BufferedWriter bw = Files.newBufferedWriter(outEnriq)) {

            String header = br.readLine();
            if (header == null) throw new IOException("Empty consolidado file");
            // ensure output header has RegistroANS;Modalidade;UF
            String outHeader = header;
            if (!outHeader.contains("RegistroANS;Modalidade;UF")) outHeader = outHeader + ";RegistroANS;Modalidade;UF";
            bw.write(outHeader + "\n");

            String line;
            Map<String, Double> sumByRazaoUf = new HashMap<>();
            Map<String, Integer> countByRazaoUf = new HashMap<>();

            // try to find indices for fields we need
            String[] hdr = header.split(";", -1);
            int cnpjI = idx(hdr, "CNPJ");
            int razI = idx(hdr, "RazaoSocial");
            int valI = idx(hdr, "ValorDespesas");
            int regI = idx(hdr, "RegistroANS");

            while ((line = br.readLine()) != null) {
                String[] cols = line.split(";", -1);
                String cnpj = cnpjI>=0 && cnpjI<cols.length ? onlyDigits(cols[cnpjI]) : "";
                String razao = razI>=0 && razI<cols.length ? cols[razI] : "";
                String valorStr = valI>=0 && valI<cols.length ? cols[valI] : "0";
                double valor = parseNumber(valorStr);

                Op match = null;
                if (cnpj != null && cnpj.length()==14) match = byCnpj.get(cnpj);
                // if CNPJ field actually contains a small numeric code, treat as registro
                if (match==null && cnpj!=null && cnpj.length()>0 && cnpj.length()<=6) match = byRegistro.get(cnpj);

                StringBuilder outLine = new StringBuilder();
                for (int i=0;i<cols.length;i++) { if (i>0) outLine.append(';'); outLine.append(cols[i]); }
                outLine.append(';');
                if (match!=null) outLine.append(match.registro); outLine.append(';');
                if (match!=null) outLine.append(match.modalidade); outLine.append(';');
                if (match!=null) outLine.append(match.uf);

                bw.write(outLine.toString() + "\n");

                String key = (razao==null?"":razao) + "__|__" + (match!=null && match.uf!=null ? match.uf : "");
                sumByRazaoUf.put(key, sumByRazaoUf.getOrDefault(key, 0.0) + valor);
                countByRazaoUf.put(key, countByRazaoUf.getOrDefault(key, 0) + 1);
            }

            // write aggregation
            try (BufferedWriter bag = Files.newBufferedWriter(outAg)) {
                bag.write("RazaoSocial;UF;Count;Total\n");
                for (Map.Entry<String, Double> e : sumByRazaoUf.entrySet()) {
                    String key = e.getKey();
                    double total = e.getValue();
                    int cnt = countByRazaoUf.getOrDefault(key, 0);
                    String[] parts = key.split("__\\|__", -1);
                    String r = parts.length>0?parts[0]:"";
                    String uf = parts.length>1?parts[1]:"";
                    bag.write(String.format(Locale.US, "%s;%s;%d;%.2f\n", r, uf, cnt, total));
                }
            }
        }

        System.out.println("Enriquecimento completo. Arquivos:");
        System.out.println(" - " + outEnriq.toString());
        System.out.println(" - " + outAg.toString());
    }

    static int idx(String[] a, String v) { for (int i=0;i<a.length;i++) if (a[i].equals(v)) return i; return -1; }
    static String safe(String[] a, int i) { if (i<0 || i>=a.length) return ""; return a[i].trim(); }
    static String onlyDigits(String s) { if (s==null) return ""; return s.replaceAll("\\D", ""); }
    static double parseNumber(String s) { if (s==null || s.isEmpty()) return 0.0; s = s.replace('.', ' ').replace(',', '.').replace(" ", ""); try { return Double.parseDouble(s); } catch (Exception ex) { return 0.0; } }

    // simple split that preserves quoted semicolons
    static String[] splitPreserve(String line) {
        java.util.List<String> out = new java.util.ArrayList<>();
        StringBuilder cur = new StringBuilder(); boolean inQ=false;
        for (int i=0;i<line.length();i++){
            char c = line.charAt(i);
            if (c=='"') { inQ = !inQ; cur.append(c); }
            else if (c==';' && !inQ) { out.add(cur.toString()); cur.setLength(0); }
            else cur.append(c);
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }
}
