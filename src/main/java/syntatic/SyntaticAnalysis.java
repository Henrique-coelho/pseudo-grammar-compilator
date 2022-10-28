package syntatic;

import lexical.Lexeme;
import lexical.LexicalAnalysis;
import lexical.LexicalException;
import lexical.TokenType;

import java.io.IOException;
import java.util.Objects;

public class SyntaticAnalysis {

    private LexicalAnalysis lex;
    private Lexeme current;

    public SyntaticAnalysis(LexicalAnalysis lex) throws LexicalException {
        this.lex = lex;
        try {
            this.current = lex.nextToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void advance() throws LexicalException {
        try {
            current = lex.nextToken();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void eat(TokenType type) throws LexicalException {
        if (type == current.type) {
            try {
                current = lex.nextToken();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showError();
        }
    }

    private void showError() {
        System.out.printf("%02d: ", lex.getLine());

        switch (current.type) {
            case INVALID_TOKEN:
                System.out.printf("Lexema inválido [%s]\n", current.token);
                break;
            case UNEXPECTED_EOF:
            case END_OF_FILE:
                System.out.printf("Fim de arquivo inesperado\n");
                break;
            default:
                System.out.printf("Lexema não esperado [%s]\n", current.token);
                break;
        }
        System.exit(1);
    }

    //program-prime ::= program "$"
    private void procProgramPrime() throws LexicalException {
        procProgram();
        eat(TokenType.END_OF_FILE);
    }

    //program ::= start decl-list-opt stmt-list exit
    private void procProgram() throws LexicalException {
//        procStart();
//        procDeclListOpt();
//        procStmtList();
//        procExit();
    }
    //decl-list-opt ::= decl-list | ""
    //decl-list ::= decl decl-list-tail | decl
    //decl-list-tail ::= decl-list | ""
    //decl ::= type ident-list ";"
    //ident-list ::= identifier ident-list-tail
    //ident-list-tail ::= "," ident-list | ""
    //type ::= int | float | string
    //stmt-list ::= stmt stmt-list-tail
    //stmt-list-tail ::= stmt-list | ""
    //stmt ::= assign-stmt ";" | if-stmt | while-stmt | read-stmt ";" | write-stmt ";"
    //assign-stmt ::= identifier "=" simple_expr
    //if-stmt ::= if condition then stmt-list end | if condition then stmt-list else stmt-list end
    //condition ::= expression
    // while-stmt ::= do stmt-list stmt-sufix
    //stmt-sufix ::= while condition end
    //read-stmt ::= scan "(" identifier ")"
    //write-stmt ::= print "(" writable ")"
    //writable ::= simple-expr | literal
    //expression ::= simple-expr | simple-expr relop simple-expr
    //simple-expr ::= term | simple-expr addop term
    //term ::= factor-a | term mulop factor-a
    //fator-a ::= factor | "!" factor | "-" factor
    //factor ::= identifier | constant | "(" expression ")"
    //relop ::= "==" | ">" | ">=" | "<" | "<=" | "<>"
    private void procRelOp() throws LexicalException {
        advance();
        if (current.type != TokenType.EQUALS && current.type != TokenType.GREATER && current.type != TokenType.GREATER_EQ &&
                current.type != TokenType.LOWER && current.type != TokenType.LOWER_EQ && current.type != TokenType.NOT_EQUALS) {
            throw new LexicalException("relop expected");
        }
    }
    //addop ::= "+" | "-" | "||"
    private void procAddOp() throws LexicalException {
        advance();
        if (current.type != TokenType.ADD && current.type != TokenType.SUB && current.type != TokenType.OR) {
            throw new LexicalException("addop expected");
        }
    }
    //mulop ::= "*" | "/" | "&&"
    private void procMulop() throws LexicalException {
        advance();
        if (current.type != TokenType.MUL && current.type != TokenType.DIV && current.type != TokenType.AND) {
            throw new LexicalException("mulop expected");
        }
    }
    //constant ::= integer_const | float_const | literal
    private void procConstant() throws LexicalException {
        //TODO precisa conferir follow
//        advance();
//        if (current.token.matches("[0-9]")) {
//            procIntegerConst();
//        } else if () {
//
//        } else if (current.token.equals("{")) {
//
//        }
    }
    //integer_const ::= digit integer_const_tail
    private void procIntegerConst() throws LexicalException {
        if(current.token.matches("[0-9]")) {
            procIntegerConstTail();
        }
    }
    //integer_const_tail ::= integer_const | ""
    private void procIntegerConstTail() throws LexicalException {
        advance();
        if(current.token.matches("[0-9]")) {
            procIntegerConst();
        }
    }
    //float_const ::= integer_const “.” integer_const
    private void procFloatConst() throws LexicalException {
        procIntegerConst();
        eat(TokenType.DOT);
        procIntegerConst();
    }
    //caractere-rept ::= caractere caractere-rept | ""
    private void procCharactereRept() throws LexicalException {
        advance();
        if (current.token  != "\n") {
            //procCharactere(); TODO conferir
            procCharactereRept();
        }
    }
    //literal ::= "{" literal-rept "}"
    private void procLiteral() throws LexicalException {
        eat(TokenType.OPEN_BRA);
        procLiteralRept();
        eat(TokenType.CLOSE_BRA);
    }
    //literal-rept ::= caractere caractere-rept
    private void procLiteralRept() throws LexicalException {
        procCharactere();
        procCharactereRept();
    }
    //identifier ::= letter-under identifier-tail
    private void procIdentifier() throws LexicalException {
        procLetterUnder();
        procIdentifierTail();
    }
    //letter-digit ::= letter | digit
    private void procLetterDigit() throws LexicalException {
        advance();
        if (!current.token.matches("[a-zA-Z]") && !current.token.matches("[0-9]")) {
            throw new LexicalException("Letra ou under_score nao encontrados");
        }
    }
    //letter-under ::= letter | _
    private void procLetterUnder() throws LexicalException {
        advance();
        if (!current.token.matches("[a-zA-Z]") && !current.token.equals("_")) {
            throw new LexicalException("Letra ou under_score nao encontrados");
        }
    }
    //identifier-tail ::= letter-digit identifier-tail | ""
    private void procIdentifierTail() throws LexicalException {
        advance();
        if (current.token.matches("[a-zA-Z]") || current.token.matches("[0-9]")) {
            procLetterDigit();
            procIdentifierTail();
        }
    }
    //letter ::= [A-za-z]
    private void procLetter() throws LexicalException {
        advance();
        if (!current.token.matches("[a-zA-Z]")){
            throw new LexicalException("Digito esperado");
        }
    }
    //digit ::= [0-9]
    private void procDigit() throws LexicalException {
        advance();
        try{
            Integer.parseInt(current.token);
        } catch (Exception e){
            throw new LexicalException("Digito esperado");
        }
    }
    //caractere ::= um dos caracteres ASCII, exceto quebra de linha
    private void procCharactere() throws LexicalException {
        advance();
        if (Objects.equals(current.token, "\n")) {
            throw new LexicalException("Caractere esperado");
        }
    }
}
