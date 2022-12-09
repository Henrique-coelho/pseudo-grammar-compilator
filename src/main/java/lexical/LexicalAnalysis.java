package lexical;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

public class LexicalAnalysis implements AutoCloseable {

    private int line;
    private SymbolTable st;
    private PushbackInputStream input;

    public LexicalAnalysis(String filename) throws LexicalException {
        try {
            input = new PushbackInputStream(new FileInputStream(filename));
        } catch (Exception e) {
            throw new LexicalException("Unable to open file");
        }

        st = new SymbolTable();
        line = 1;
    }

    public void close() throws IOException {
        input.close();
    }

    public int getLine() {
        return this.line;
    }

    public Lexeme nextToken() throws LexicalException, IOException {
        Lexeme lex = new Lexeme("", TokenType.END_OF_FILE);

        int state = 1;
        while (state != 200 && state != 201) {

            int c = getc();
            char vc = (char) c; // temp

            switch (state){
                // state == 200, tipo reconhecido
                // state == 201, tipo não reconhecido ainda
                case 1: // Estado inicial
                    if(c == ' ' || c == '\t' || c == '\r'){
                        state = 1;
                    } else if(c=='\n') {
                        line++;
                        System.out.println("Line "+line+"!");
                        state = 1;
                    } else if(c=='*') {
                        state = 2;
                    } else if(c=='/') {
                        state = 5;
                    } else if(Character.isLetter(c)||c=='_') {
                        lex.token += (char) c;
                        state = 7;
                    } else if(Character.isDigit(c)) {
                        lex.token += (char) c;
                        state = 8;
                    } else if(c=='=' || c=='>'){
                        lex.token += (char) c;
                        state = 11;
                    } else if(c=='<'){
                        lex.token += (char) c;
                        state = 12;
                    } else if(c=='+' || c=='-' || c=='/' || c=='(' || c==')' || c=='.' || c==';' || c=='!' || c==','){
                        lex.token += (char) c;
                        state = 201;
                    } else if(c=='|') {
                        lex.token += (char) c;
                        state = 13;
                    } else if(c=='&') {
                        lex.token += (char) c;
                        state = 14;
                    } else if(c=='{') {
                        state = 15;
                    } else if(c == -1) { // EOF
                        lex.type = TokenType.END_OF_FILE;
                        state = 200;
                    } else {
                        lex.token += (char) c;
                        lex.type = TokenType.INVALID_TOKEN;
                        state = 200;
                    }
                    break;
                case 2: // Multiplicação
                    ungetc(c);
                    lex.token += '*';
                    state = 201;
                    break;
                case 3: // Comentário de mais de uma linha
                    if (c == '*') {
                        state = 4;
                    } else if (c == '\n') {
                        line++;
                        System.out.println("Line "+line+"!");
                        state = 4;
                    }
                    break;
                case 4: // Possível final de comentário de mais de uma linha
                    if (c == '/') {
                        state = 1;
                    } else {
                        state = 3;
                    }
                    break;
                case 5: // Possível comentário ou divisão
                    if (c == '/'){
                        state = 6;
                    } else if (c == '*') {
                        state = 3;
                    } else {
                        ungetc(c);
                        lex.token += '/';
                        state = 201;
                    }
                    break;
                case 6: // Comentário normal
                    if (c == '\n') {
                        line ++;
                        System.out.println("Line "+line+"!");
                        state = 1;
                    } else if(c == -1) { // EOF
                        lex.type = TokenType.END_OF_FILE;
                        state = 200;
                    }
                    break;
                case 7: // Identificador ou Palavra Chave
                    if (Character.isDigit(c) || Character.isLetter(c)){
                        lex.token += (char) c;
                    } else {
                        ungetc(c);
                        state = 201;
                    }
                    break;
                case 8: // Número
                    if (Character.isDigit(c)){
                        lex.token += (char) c;
                    } else if(c=='.') {
                        lex.token += (char) c;
                        state = 9;
                    } else {
                        ungetc(c);
                        lex.type = TokenType.INTEGER;
                        state = 200;
                    }
                    break;
                case 9: // Float
                    if (Character.isDigit(c)){
                        lex.token += (char) c;
                        state = 10;
                    }  else {
                        ungetc(c);
                        lex.type = TokenType.INVALID_TOKEN;
                        state = 200;
                    }
                    break;
                case 10:
                    if (Character.isDigit(c)){
                        lex.token += (char) c;
                    } else {
                        ungetc(c);
                        lex.type = TokenType.FLOAT;
                        state = 200;
                    }
                    break;
                case 11: // ("=" ou "==") ou (">" ou ">=")
                    if(c=='='){
                        lex.token += (char) c;
                    } else {
                        ungetc(c);
                        state = 200;
                    }
                    state = 201;
                    break;
                case 12:
                    if(c=='>' || c=='='){ // ("<" ou "<=" ou "<>")
                        lex.token += (char) c;
                    } else {
                        ungetc(c);
                    }
                    state = 201;
                    break;
                case 13:
                    if(c=='|'){ // "||"
                        lex.token += (char) c;
                        state = 201;
                    } else {
                        ungetc(c);
                        lex.type = TokenType.INVALID_TOKEN;
                        state = 200;
                    }
                    break;
                case 14: // "&&"
                    if(c=='&'){
                        lex.token += (char) c;
                        state = 201;
                    } else {
                        ungetc(c);
                        lex.type = TokenType.INVALID_TOKEN;
                        state = 200;
                    }
                    break;
                case 15: // String
                    if(c=='}'){
                        lex.type = TokenType.STRING;
                        state = 200;
                    } else if(c!='\n'){
                        lex.token += (char) c;
                    }
                    break;
                case 16: // Fim do comentario de multi linhas
                    if (c=='/'){
                        state = 200;
                    }
            }
        }

        if (state == 201)
            lex.type = st.find(lex.token);

        //System.out.println("Token: ( '" + lex.token + " '), Type: " + lex.type + ", Line: " + getLine());
        return lex;
    }

    private int getc() throws LexicalException {
        try {
            return input.read();
        } catch (Exception e) {
            throw new LexicalException("Unable to read file");
        }
    }

    private void ungetc(int c) throws LexicalException {
        if (c != -1) {
            try {
                input.unread(c);
            } catch (Exception e) {
                throw new LexicalException("Unable to ungetc");
            }
        }
    }
}