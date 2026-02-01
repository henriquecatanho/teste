import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UnmatchedResolver {

    static class Op { String registro, razao, cnpj, modalidade, uf; }

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println("Usage: java UnmatchedResolver <consolidado_enriquecido.csv> <Relatorio_cadop.csv> <out_prefix>");
            return;
        }
        String inputConsolidado = args[0];
        String relatorio = args[1];
        String prefix = args[2];

        System.out.println("Loading operadoras from: " + relatorio);
        List<Op> ops = loadOps(relatorio);

        Map<String, Op> byRegistro = new HashMap<>();
        Map<String, Op> byCnpj = new HashMap<>();
        Map<String, List<Op>> byRazaoNorm = new HashMap<>();

        for (Op o : ops) {
            if (o.registro != null && !o.registro.isEmpty()) byRegistro.put(o.registro, o);
            String cdigits = onlyDigits(o.cnpj);
            if (!cdigits.isEmpty()) byCnpj.put(cdigits, o);
            String rn = norm(o.razao);
            byRazaoNorm.computeIfAbsent(rn, k -> new ArrayList<>()).add(o);
        }

        System.out.println("Operators loaded: " + ops.size());

        Path in = Paths.get(inputConsolidado);
        Path out = Paths.get(prefix + "_corrected.csv");
        Path report = Paths.get(prefix + "_heuristic_matches.csv");

        try (BufferedReader br = Files.newBufferedReader(in);
             BufferedWriter bw = Files.newBufferedWriter(out);
             BufferedWriter brpt = Files.newBufferedWriter(report)) {

            String header = br.readLine();
            if (header == null) throw new IllegalStateException("Empty input file");

            // ensure output header has added columns
            String outHeader = header;
            if (!outHeader.contains("RegistroANS;Modalidade;UF")) outHeader = outHeader + ";RegistroANS;Modalidade;UF";
            bw.write(outHeader + "\n");
            brpt.write("lineNumber;originalRegistroANS;heuristic;matchedRegistro;matchedRazao;matchedUF;score\n");

            String[] hdr = header.split(";", -1);
            int regI = indexOf(hdr, "RegistroANS");
            int razI = indexOf(hdr, "RazaoSocial");
            int cnpjI = indexOf(hdr, "CNPJ");

            String line;
            long ln = 1;
            int resolved = 0, unresolved = 0;

            while ((line = br.readLine()) != null) {
                ln++;
                String[] cols = line.split(";", -1);
                String registro = regI >= 0 && regI < cols.length ? cols[regI].trim() : "";
                if (registro != null && !registro.isEmpty()) {
                    // already has registro, preserve and write
                    bw.write(ensureAppended(cols, null) + "\n");
                    continue;
                }

                String razao = razI >= 0 && razI < cols.length ? cols[razI].trim() : "";
                String cnpj = cnpjI >= 0 && cnpjI < cols.length ? cols[cnpjI].trim() : "";

                boolean matched = false;

                // Heuristic 1: if CNPJ field contains a small numeric value, treat it as REG_ANS
                String cnpjDigits = onlyDigits(cnpj);
                if (!matched && cnpjDigits.length() > 0 && cnpjDigits.length() <= 6) {
                    Op o = byRegistro.get(cnpjDigits);
                    if (o != null) {
                        bw.write(ensureAppended(cols, o) + "\n");
                        brpt.write(String.format("%d;%s;byRegistro;%s;%s;%s;%.3f\n", ln, registro, o.registro, o.razao, o.uf, 1.0));
                        resolved++; matched = true;
                    }
                }

                // Heuristic 1b: if cnpjDigits looks like a full CNPJ
                if (!matched && cnpjDigits.length() == 14) {
                    Op o = byCnpj.get(cnpjDigits);
                    if (o != null) {
                        bw.write(ensureAppended(cols, o) + "\n");
                        brpt.write(String.format("%d;%s;byCNPJ;%s;%s;%s;%.3f\n", ln, registro, o.registro, o.razao, o.uf, 1.0));
                        resolved++; matched = true;
                    }
                }

                // Heuristic 2: exact normalized razao
                if (!matched) {
                    String rn = norm(razao);
                    List<Op> list = byRazaoNorm.getOrDefault(rn, Collections.emptyList());
                    if (list.size() == 1) {
                        Op o = list.get(0);
                        bw.write(ensureAppended(cols, o) + "\n");
                        brpt.write(String.format("%d;%s;exactRazao;%s;%s;%s;%.3f\n", ln, registro, o.registro, o.razao, o.uf, 1.0));
                        resolved++; matched = true;
                    }
                }

                // Heuristic 3: fuzzy razao (Levenshtein)
                if (!matched) {
                    String nraz = norm(razao);
                    Op best = null; int bestD = Integer.MAX_VALUE;
                    for (Op o : ops) {
                        int d = levenshtein(nraz, norm(o.razao));
                        if (d < bestD) { bestD = d; best = o; }
                    }
                    if (best != null) {
                        double ratio = (double)bestD / Math.max(1, Math.max(nraz.length(), norm(best.razao).length()));
                        if (ratio <= 0.25) {
                            bw.write(ensureAppended(cols, best) + "\n");
                            brpt.write(String.format(Locale.US, "%d;%s;fuzzyRazao;%s;%s;%s;%.3f\n", ln, registro, best.registro, best.razao, best.uf, ratio));
                            resolved++; matched = true;
                        }
                    }
                }

                if (!matched) {
                    bw.write(ensureAppended(cols, null) + "\n");
                    brpt.write(String.format("%d;%s;none;;;\n", ln, registro));
                    unresolved++;
                }
            }

            System.out.println("Resolved: " + resolved + ", Unresolved: " + unresolved);
        }
    }

    static String ensureAppended(String[] cols, Op o) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < cols.length; i++) { if (i>0) sb.append(';'); sb.append(cols[i]); }
        sb.append(';');
        if (o != null) sb.append(o.registro==null?"":o.registro);
        sb.append(';');
        if (o != null) sb.append(o.modalidade==null?"":o.modalidade);
        sb.append(';');
        if (o != null) sb.append(o.uf==null?"":o.uf);
        return sb.toString();
    }

    static int indexOf(String[] a, String v) { for (int i=0;i<a.length;i++) if (a[i].equals(v)) return i; return -1; }

    static List<Op> loadOps(String rel) throws Exception {
        List<Op> out = new ArrayList<>();
        try (BufferedReader br = Files.newBufferedReader(Paths.get(rel))) {
            String header = br.readLine();
            if (header == null) return out;
            String[] hdr = header.split(";", -1);
            int regI = indexOf(hdr, "REGISTRO_OPERADORA");
            int cnpjI = indexOf(hdr, "CNPJ");
            int razI = indexOf(hdr, "Razao_Social");
            int modI = indexOf(hdr, "Modalidade");
            int ufI = indexOf(hdr, "UF");

            String line;
            while ((line = br.readLine()) != null) {
                String[] cols = splitPreserve(line);
                Op o = new Op();
                o.registro = strip(cols, regI);
                o.cnpj = strip(cols, cnpjI);
                o.razao = strip(cols, razI);
                o.modalidade = strip(cols, modI);
                o.uf = strip(cols, ufI);
                out.add(o);
            }
        }
        return out;
    }

    static String strip(String[] a, int i) { if (i<0 || i>=a.length) return ""; String s = a[i].trim(); if (s.startsWith("\"") && s.endsWith("\"")) s = s.substring(1, s.length()-1); return s; }

    static String[] splitPreserve(String line) {
        List<String> out = new ArrayList<>(); StringBuilder cur = new StringBuilder(); boolean inQ=false;
        for (int i=0;i<line.length();i++){
            char c = line.charAt(i);
            if (c=='"') { inQ = !inQ; cur.append(c); }
            else if (c==';' && !inQ) { out.add(cur.toString()); cur.setLength(0); }
            else cur.append(c);
        }
        out.add(cur.toString());
        return out.toArray(new String[0]);
    }

    static String onlyDigits(String s) { if (s==null) return ""; return s.replaceAll("\\D", ""); }

    static String norm(String s) { if (s==null) return ""; return s.replaceAll("[^\\p{L}\\p{Nd}]+", " ").toLowerCase().trim(); }

    static int levenshtein(String a, String b) {
        if (a == null) a = ""; if (b == null) b = "";
        int[][] dp = new int[a.length()+1][b.length()+1];
        for (int i=0;i<=a.length();i++) dp[i][0]=i;
        for (int j=0;j<=b.length();j++) dp[0][j]=j;
        for (int i=1;i<=a.length();i++) for (int j=1;j<=b.length();j++){
            int cost = a.charAt(i-1)==b.charAt(j-1)?0:1;
            dp[i][j] = Math.min(Math.min(dp[i-1][j]+1, dp[i][j-1]+1), dp[i-1][j-1]+cost);
        }
        return dp[a.length()][b.length()];
    }
}
