package syntatic;

import lexical.Lexeme;
import lexical.LexicalAnalysis;
import lexical.LexicalException;
import lexical.TokenType;
import semantic.Axiom;
import semantic.AxiomTable;
import semantic.SemanticException;

import java.io.IOException;

public class SyntaticAnalysis {

    private LexicalAnalysis lex;
    private Lexeme current;
    private AxiomTable axs;

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

    private String eat(TokenType type) throws LexicalException {
        Lexeme food = current;
        if (type == food.type) {
            try {
                current = lex.nextToken();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            showError();
        }
        return food.token;
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

    public void start() throws LexicalException,SemanticException{
        System.out.println(" *** Program Started *** ");
        procProgram();
        eat(TokenType.END_OF_FILE);
        System.out.println(" *** Program Ended ***");
    }

    //program ::= start [decl-list] stmt-list exit
    private void procProgram() throws LexicalException,SemanticException {
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.START);
        while (current.type == TokenType.STRING_KW || current.type == TokenType.INTEGER_KW || current.type == TokenType.FLOAT_KW){
            procDeclList();
        }
        procStmtList();
        eat(TokenType.EXIT);

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //decl-list ::= decl {decl}
    private void procDeclList() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procDecl();
        while (current.type == TokenType.STRING_KW || current.type == TokenType.INTEGER_KW || current.type == TokenType.FLOAT_KW){
            procDecl();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }
    //decl ::= type ident-list ";"
    private void procDecl() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procType();
        procIdList();
        eat(TokenType.SEMICOLON);

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //ident-list ::= identifier {"," identifier}
    private void procIdList() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procId();
        while(current.type == TokenType.COMMA){
            advance();
            procId();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //type ::= int | float | string
    private void procType() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        if(current.type == TokenType.STRING_KW){
            advance();
        } else if(current.type == TokenType.INTEGER_KW){
            advance();
        } else if(current.type == TokenType.FLOAT_KW) {
            advance();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }


    //stmt-list ::= stmt | {stmt}
    private void procStmtList() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procStatement();
        while(current.type == TokenType.ID ||
                current.type == TokenType.IF ||
                current.type == TokenType.DO ||
                current.type == TokenType.SCAN ||
                current.type == TokenType.PRINT){
            procStatement();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //stmt ::= assign-stmt ";" | if-stmt | while-stmt | read-stmt ";" | write-stmt ";"
    private void procStatement() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        if(current.type == TokenType.ID) {
            procAssign();
            eat(TokenType.SEMICOLON);
        } else if(current.type == TokenType.IF){
            procIf();
        } else if(current.type == TokenType.DO) {
            procWhile();
        } else if(current.type == TokenType.SCAN) {
            procRead();
            eat(TokenType.SEMICOLON);
        } else if(current.type == TokenType.PRINT) {
            procWrite();
            eat(TokenType.SEMICOLON);
        } else {
            showError();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //assign-stmt ::= identifier "=" simple_expr
    private void procAssign() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procId();
        eat(TokenType.ASSIGN);
        procSimpleExpr();

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }
    //if-stmt ::= if condition then stmt-list end | if condition then stmt-list else stmt-list end
    private void procIf() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.IF);
        procCond();
        eat(TokenType.THEN);
        procStmtList();
        if(current.type == TokenType.END){
            advance();
        } else if(current.type == TokenType.ELSE){
            advance();
            procStmtList();
            eat(TokenType.END);
        } else {
            showError();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //condition ::= expression
    private void procCond() throws  LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procExpr();

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    // while-stmt ::= do stmt-list stmt-sufix
    private void procWhile() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.DO);
        procStmtList();
        procSufix();

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //stmt-sufix ::= while condition end
    private void procSufix() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.WHILE);
        procCond();
        eat(TokenType.END);

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //read-stmt ::= scan "(" identifier ")"
    private void procRead() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.SCAN);
        eat(TokenType.OPEN_BRA);
        procId();
        eat(TokenType.CLOSE_BRA);

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //write-stmt ::= print "(" writable ")"
    private void procWrite() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.PRINT);
        eat(TokenType.OPEN_BRA);
        procWritable();
        eat(TokenType.CLOSE_BRA);

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //writable ::= simple-expr | literal
    private void procWritable() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        if(current.type == TokenType.STRING){
            procLiteral();
        } else {
            procSimpleExpr();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //expression ::= simple-expr | simple-expr relop simple-expr
    private Axiom procExpr() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procSimpleExpr();
        while(current.type == TokenType.EQUALS ||
                current.type == TokenType.GREATER ||
                current.type == TokenType.GREATER_EQ ||
                current.type == TokenType.LOWER ||
                current.type == TokenType.LOWER_EQ ||
                current.type == TokenType.NOT_EQUALS){
            procRelOp();
            procSimpleExpr();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
        return null;
    }

    //simple-expr ::= term | simple-expr addop term
    private void procSimpleExpr() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procTerm();
        while(current.type == TokenType.ADD || current.type == TokenType.SUB || current.type == TokenType.OR){
            procAddOp();
            procSimpleExpr();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //term ::= factor-a | term mulop factor-a
    private void procTerm() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procFactorAct();
        while(current.type == TokenType.MUL || current.type == TokenType.DIV || current.type == TokenType.AND){
            procMulop();
            procTerm();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //fator-a ::= factor | "!" factor | "-" factor
    private Axiom procFactorAct() throws  LexicalException,SemanticException{
        if(current.type == TokenType.ID || current.type == TokenType.INTEGER || current.type == TokenType.FLOAT || current.type == TokenType.STRING || current.type == TokenType.OPEN_BRA){
            return procFactor();
        } else if(current.type==TokenType.EXCLAMATION){
            eat(TokenType.EXCLAMATION);
            procFactor();
            //TODO modificar classe axiom para possuir valores flexiveis
            //TODO prosseguir a modificar todos os procs
        } else if(current.type==TokenType.SUB){
            eat(TokenType.SUB);
            procFactor();
        }
        return null;
    }

    //factor ::= identifier | constant | "(" expression ")"
    private Axiom procFactor() throws LexicalException,SemanticException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        if(current.type == TokenType.ID){
            String id = procId();
            if(axs.contains(id))
                return axs.get(id);
            else
                throw new SemanticException(id+" não está inserido na tabela");
        } else if(current.type == TokenType.INTEGER || current.type == TokenType.FLOAT || current.type == TokenType.STRING){
            return procConstant();
        } else if(current.type == TokenType.OPEN_BRA){
            eat(TokenType.OPEN_BRA);
            Axiom ax = procExpr();
            eat(TokenType.CLOSE_BRA);
            return ax;
        } else {
            showError();
        }
        return null;
    }

    //relop ::= "==" | ">" | ">=" | "<" | "<=" | "<>"
    private String procRelOp() throws LexicalException {
        if (current.type == TokenType.EQUALS){
            return eat(TokenType.EQUALS);
        } else if (current.type == TokenType.GREATER) {
            return eat(TokenType.GREATER);
        } else if (current.type == TokenType.GREATER_EQ) {
            return eat(TokenType.GREATER_EQ);
        } else if (current.type == TokenType.LOWER) {
            return eat(TokenType.LOWER);
        } else if (current.type == TokenType.LOWER_EQ) {
            return eat(TokenType.LOWER_EQ);
        } else if(current.type == TokenType.NOT_EQUALS) {
            return eat(TokenType.NOT_EQUALS);
        } else {
            showError();
        }
        return null;
    }

    //addop ::= "+" | "-" | "||"
    private String procAddOp() throws LexicalException {
        if (current.type == TokenType.ADD) {
            return eat(TokenType.ADD);
        } else if (current.type == TokenType.SUB) {
            return eat(TokenType.SUB);
        } else if (current.type == TokenType.OR) {
            return eat(TokenType.OR);
        } else {
            showError();
        }
        return null;
    }
    //mulop ::= "*" | "/" | "&&"
    private String procMulop() throws LexicalException {
        if (current.type == TokenType.MUL) {
            return eat(TokenType.MUL);
        } else if (current.type == TokenType.DIV) {
            return eat(TokenType.DIV);
        } else if (current.type == TokenType.AND) {
            return eat(TokenType.AND);
        } else {
            showError();
        }
        return null;
    }
    //constant ::= integer_const | float_const | literal
    private Axiom procConstant() throws LexicalException {
        if (current.type == TokenType.INTEGER) {
            return procIntegerConst();
        } else if (current.type == TokenType.FLOAT) {
            return procFloatConst();
        } else if (current.type == TokenType.STRING) {
            return procLiteral();
        } else {
            showError();
        }
        return null;
    }
    //integer_const ::= digit integer_const_tail
    private Axiom procIntegerConst() throws LexicalException {
        String token = eat(TokenType.INTEGER);
        return new Axiom("integer", token);
    }

    //float_const ::= integer_const “.” integer_const
    private Axiom procFloatConst() throws LexicalException {
        String token = eat(TokenType.FLOAT);
        return new Axiom("float", token);
    }

    //literal ::= "{" literal-rept "}"
    private Axiom procLiteral() throws LexicalException {
        String token = eat(TokenType.STRING);
        return new Axiom("string", token);
    }

    //identifier ::= letter-under identifier-tail
    private String procId() throws LexicalException {
        String token = eat(TokenType.ID);
        return token;
    }

    //letter-digit ::= letter | digit (sem proc)
    //letter-under ::= letter | _ (sem proc)
    //letter ::= [A-za-z] (sem proc)
    //digit ::= [0-9] (sem proc)
    //caractere ::= um dos caracteres ASCII, exceto quebra de linha (sem proc)
}
