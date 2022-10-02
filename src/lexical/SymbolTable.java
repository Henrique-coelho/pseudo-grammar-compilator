package lexical;

import java.util.HashMap;
import java.util.Map;

public class SymbolTable {
    private Map<String, TokenType> st;

    public SymbolTable() {
        st = new HashMap<String, TokenType>();

        // SYMBOLS
        st.put("(", TokenType.OPEN_BRA);
        st.put(")", TokenType.CLOSE_BRA);
        st.put("{", TokenType.OPEN_PAR);
        st.put("}", TokenType.CLOSE_PAR);
        st.put(";", TokenType.SEMICOLON);
        st.put(",", TokenType.COMMA);
        st.put("=", TokenType.ASSIGN);
        st.put(".", TokenType.DOT);
        st.put("!", TokenType.EXCLAMATION);

        // OPERATORS
        st.put("==", TokenType.EQUALS);
        st.put("<>", TokenType.NOT_EQUALS);
        st.put(">", TokenType.GREATER);
        st.put(">=", TokenType.GREATER_EQ);
        st.put("<", TokenType.LOWER);
        st.put("<=", TokenType.LOWER_EQ);

        st.put("+", TokenType.ADD);
        st.put("-", TokenType.SUB);
        st.put("||", TokenType.OR);

        st.put("*", TokenType.MUL);
        st.put("/", TokenType.DIV);
        st.put("&&", TokenType.AND);

        // KEYWORDS
        st.put("start", TokenType.START);
        st.put("exit", TokenType.EXIT);
        st.put("if", TokenType.IF);
        st.put("then", TokenType.THEN);
        st.put("else", TokenType.ELSE);
        st.put("end", TokenType.END);
        st.put("do", TokenType.DO);
        st.put("while", TokenType.WHILE);
        st.put("scan", TokenType.SCAN);
        st.put("print", TokenType.PRINT);
        st.put("int", TokenType.INTEGER_KW);
        st.put("float", TokenType.FLOAT_KW);
        st.put("string", TokenType.STRING_KW);
    }

    public boolean contains(String token) {
        return st.containsKey(token);
    }

    public TokenType find(String token) {
        return this.contains(token) ?
                st.get(token) : TokenType.ID;
    }
}