import java.nio.file.Files;
import java.nio.file.Paths;

public class Main {
    public static void main(String[] args) {
        Assembler assembler = new Assembler();
        String code = "";

        try {
            code = new String(Files.readAllBytes(Paths.get("C:\\Users\\AghoyPandaaa\\IdeaProjects\\JASM\\src\\test.asm")));
        } catch (Exception e) {
            System.out.println("Error reading file: " + e.getMessage());
            return;
        }

        assembler.runCode(code);
    }
}