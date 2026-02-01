import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ExtractInvalidCNPJ {
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.err.println("Usage: java ExtractInvalidCNPJ <consolidado.csv> <out_invalid.csv>");
            return;
        }
        Path in = Paths.get(args[0]);
        Path out = Paths.get(args[1]);

        try (BufferedReader br = Files.newBufferedReader(in);
             BufferedWriter bw = Files.newBufferedWriter(out)) {

            String header = br.readLine();
            if (header == null) return;
            bw.write(header + "\n");

            String[] hdr = header.split(";", -1);
            int cnpjI = -1;
            for (int i=0;i<hdr.length;i++) if (hdr[i].equals("CNPJ")) { cnpjI=i; break; }

            String line; long total=0, invalid=0;
            while ((line = br.readLine()) != null) {
                total++;
                String[] cols = line.split(";", -1);
                String cnpj = cnpjI>=0 && cnpjI<cols.length ? onlyDigits(cols[cnpjI]) : "";
                if (!isValidCNPJ(cnpj)) {
                    bw.write(line + "\n");
                    invalid++;
                }
            }
            System.out.println("Processed="+total+" invalid="+invalid);
        }
    }

    static String onlyDigits(String s){ if (s==null) return ""; return s.replaceAll("\\D", ""); }

    static boolean isValidCNPJ(String cnpj) {
        if (cnpj==null) return false;
        cnpj = cnpj.replaceAll("\\D", "");
        if (cnpj.length()!=14) return false;
        try {
            int[] peso1 = {5,4,3,2,9,8,7,6,5,4,3,2};
            int[] peso2 = {6,5,4,3,2,9,8,7,6,5,4,3,2};
            int sum=0;
            for (int i=0;i<12;i++) sum += Character.getNumericValue(cnpj.charAt(i)) * peso1[i];
            int r = sum % 11; int d1 = (r<2)?0:11-r;
            sum=0;
            for (int i=0;i<13;i++) sum += Character.getNumericValue(cnpj.charAt(i)) * peso2[i];
            r = sum % 11; int d2 = (r<2)?0:11-r;
            return d1 == Character.getNumericValue(cnpj.charAt(12)) && d2 == Character.getNumericValue(cnpj.charAt(13));
        } catch (Exception e) { return false; }
    }
}
