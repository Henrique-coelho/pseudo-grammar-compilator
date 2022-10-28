import lexical.LexicalAnalysis;
import lexical.LexicalException;
import lexical.TokenType;
import syntatic.SyntaticAnalysis;

public class Main {
    public static void main(String[] args) throws LexicalException {
        System.out.println(args[0]);
        if (args.length != 1) {
            System.out.println("Not enough arguments!");
            return;
        }

        try (LexicalAnalysis l = new LexicalAnalysis(args[0])) {
            SyntaticAnalysis s = new SyntaticAnalysis(l);
            s.start();
        } catch (Exception e) {
            System.err.println("Internal error: " + e.getMessage());
        }
    }

    private static boolean checkType(TokenType type) {
        return !(type == TokenType.END_OF_FILE ||
                type == TokenType.INVALID_TOKEN ||
                type == TokenType.UNEXPECTED_EOF);
    }
}

