import java.io.*;
import java.nio.charset.StandardCharsets;

public class Main {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.out.println("Please write input and output files as arguments");
            return;
        }
        try (ElfReader reader = new ElfReader(args[0])) {
            BufferedWriter writer;
            if (args.length >= 2) {
                writer = new BufferedWriter(new FileWriter(args[1], StandardCharsets.UTF_8));
            } else {
                writer = new BufferedWriter(new OutputStreamWriter(System.out));
            }
            try {
                reader.disassemble(writer);
            } catch(IOException e){
                System.out.println("Output exception: " + e.getMessage());
            } finally {
                writer.close();
            }
        } catch(FileNotFoundException e){
            System.out.println("Input file not found");
        } catch(IOException e){
            System.out.println("Input exception: " + e.getMessage());
        }
    }
}
