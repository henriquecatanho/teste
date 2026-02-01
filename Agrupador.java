import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class Agrupador {

    static class Stat {
        double sum=0.0;
        double sumsq=0.0;
        int count=0;
        Set<String> trimestres = new HashSet<>();
        void add(double v, String t) { sum+=v; sumsq+=v*v; count++; if (t!=null && !t.isEmpty()) trimestres.add(t); }
        double mean() { return count==0?0:sum/count; }
        double meanPerTrim(){ int n = trimestres.size(); return n==0?0:sum/n; }
        double stddev(){ if (count<=1) return 0.0; double var=(sumsq - (sum*sum)/count)/(count-1); return var<=0?0:Math.sqrt(var); }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java Agrupador <consolidado.csv> <out_prefix>");
            return;
        }
        Path in = Paths.get(args[0]);
        String prefix = args[1];
        Path out = Paths.get(prefix + "_despesas_agregadas.csv");

        Map<String, Stat> stats = new HashMap<>();

        try (BufferedReader br = Files.newBufferedReader(in)){
            String header = br.readLine();
            if (header==null) throw new IllegalStateException("empty file");
            String[] hdr = header.split(";", -1);
            int razI = idx(hdr, "RazaoSocial");
            int ufI = idx(hdr, "UF");
            int valI = idx(hdr, "ValorDespesas");
            int triI = idx(hdr, "Trimestre");

            String line;
            long lines=0;
            while ((line=br.readLine())!=null){
                lines++;
                String[] cols = line.split(";", -1);
                String raz = razI>=0 && razI<cols.length ? cols[razI] : "";
                String uf = ufI>=0 && ufI<cols.length ? cols[ufI] : "";
                String valS = valI>=0 && valI<cols.length ? cols[valI] : "0";
                String tri = triI>=0 && triI<cols.length ? cols[triI] : "";
                double v = parseNumber(valS);
                String key = (raz==null?"":raz) + "__|__" + (uf==null?"":uf);
                Stat s = stats.computeIfAbsent(key, k->new Stat());
                s.add(v, tri);
            }

            try (BufferedWriter bw = Files.newBufferedWriter(out)){
                bw.write("RazaoSocial;UF;Count;Total;MeanPerTrim;StdDev\n");
                stats.entrySet().stream().sorted((a,b)->Double.compare(b.getValue().sum,a.getValue().sum)).forEach(e->{
                    String key = e.getKey();
                    Stat s = e.getValue();
                    String[] parts = key.split("__\\|__", -1);
                    String raz = parts.length>0?parts[0]:"";
                    String uf = parts.length>1?parts[1]:"";
                    try {
                        bw.write(String.format(Locale.US, "%s;%s;%d;%.2f;%.2f;%.2f\n", raz, uf, s.count, s.sum, s.meanPerTrim(), s.stddev()));
                    } catch (Exception ex) {}
                });
            }

            System.out.println("Processed lines: " + lines + ", groups: " + stats.size());
            System.out.println("Wrote: " + out.toString());
        }
    }

    static int idx(String[] a, String v) { for (int i=0;i<a.length;i++) if (a[i].equals(v)) return i; return -1; }
    static double parseNumber(String s){ if (s==null||s.isEmpty()) return 0.0; s=s.replace('.', ' ').replace(',','.').replace(" ",""); try { return Double.parseDouble(s);}catch(Exception e){return 0.0;} }
}
