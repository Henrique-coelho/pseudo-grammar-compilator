package syntatic;

import lexical.Lexeme;
import lexical.LexicalAnalysis;
import lexical.LexicalException;
import lexical.TokenType;

import java.io.IOException;

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

    public void start() throws LexicalException{
        System.out.println(" *** Program Started *** ");
        procProgram();
        eat(TokenType.END_OF_FILE);
        System.out.println(" *** Program Ended ***");
    }

    //program ::= start [decl-list] stmt-list exit
    private void procProgram() throws LexicalException {
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
    private void procStmtList() throws LexicalException{
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
    private void procStatement() throws LexicalException{
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
    private void procAssign() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procId();
        eat(TokenType.ASSIGN);
        procSimpleExpr();

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }
    //if-stmt ::= if condition then stmt-list end | if condition then stmt-list else stmt-list end
    private void procIf() throws LexicalException{
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
    private void procCond() throws  LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procExpr();

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    // while-stmt ::= do stmt-list stmt-sufix
    private void procWhile() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.DO);
        procStmtList();
        procSufix();

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //stmt-sufix ::= while condition end
    private void procSufix() throws LexicalException{
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
    private void procWrite() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.PRINT);
        eat(TokenType.OPEN_BRA);
        procWritable();
        eat(TokenType.CLOSE_BRA);

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //writable ::= simple-expr | literal
    private void procWritable() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        if(current.type == TokenType.STRING){
            procLiteral();
        } else {
            procSimpleExpr();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //expression ::= simple-expr | simple-expr relop simple-expr
    private void procExpr() throws LexicalException{
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
    }

    //simple-expr ::= term | simple-expr addop term
    private void procSimpleExpr() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procTerm();
        while(current.type == TokenType.ADD || current.type == TokenType.SUB || current.type == TokenType.OR){
            procAddOp();
            procSimpleExpr();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //term ::= factor-a | term mulop factor-a
    private void procTerm() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        procFactorAct();
        while(current.type == TokenType.MUL || current.type == TokenType.DIV || current.type == TokenType.AND){
            procMulop();
            procTerm();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //fator-a ::= factor | "!" factor | "-" factor
    private void procFactorAct() throws  LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        if(current.type == TokenType.ID || current.type == TokenType.INTEGER || current.type == TokenType.FLOAT || current.type == TokenType.STRING || current.type == TokenType.OPEN_BRA){
            procFactor();
        } else if(current.type==TokenType.EXCLAMATION){
            eat(TokenType.EXCLAMATION);
            procFactor();
        } else if(current.type==TokenType.SUB){
            eat(TokenType.SUB);
            procFactor();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //factor ::= identifier | constant | "(" expression ")"
    private void procFactor() throws LexicalException{
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        if(current.type == TokenType.ID){
            procId();
        } else if(current.type == TokenType.INTEGER || current.type == TokenType.FLOAT || current.type == TokenType.STRING){
            procConstant();
        } else if(current.type == TokenType.OPEN_BRA){
            eat(TokenType.OPEN_BRA);
            procExpr();
            eat(TokenType.CLOSE_BRA);
        } else {
            showError();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //relop ::= "==" | ">" | ">=" | "<" | "<=" | "<>"
    private void procRelOp() throws LexicalException {
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        if (current.type == TokenType.EQUALS){
            advance();
        } else if (current.type == TokenType.GREATER) {
            advance();
        } else if (current.type == TokenType.GREATER_EQ) {
            advance();
        } else if (current.type == TokenType.LOWER) {
            advance();
        } else if (current.type == TokenType.LOWER_EQ) {
            advance();
        } else if(current.type == TokenType.NOT_EQUALS) {
            advance();
        } else {
            showError();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //addop ::= "+" | "-" | "||"
    private void procAddOp() throws LexicalException {
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        if (current.type == TokenType.ADD) {
            eat(TokenType.ADD);
        } else if (current.type == TokenType.SUB) {
            eat(TokenType.SUB);
        } else if (current.type == TokenType.OR) {
            eat(TokenType.OR);
        } else {
            showError();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }
    //mulop ::= "*" | "/" | "&&"
    private void procMulop() throws LexicalException {
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        if (current.type == TokenType.MUL) {
            eat(TokenType.MUL);
        } else if (current.type == TokenType.DIV) {
            eat(TokenType.DIV);
        } else if (current.type == TokenType.AND) {
            eat(TokenType.AND);
        } else {
            showError();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }
    //constant ::= integer_const | float_const | literal
    private void procConstant() throws LexicalException {
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        if (current.type == TokenType.INTEGER) {
            procIntegerConst();
        } else if (current.type == TokenType.FLOAT) {
            procFloatConst();
        } else if (current.type == TokenType.STRING) {
            procLiteral();
        } else {
            showError();
        }

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }
    //integer_const ::= digit integer_const_tail
    private void procIntegerConst() throws LexicalException {
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.INTEGER);

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //float_const ::= integer_const “.” integer_const
    private void procFloatConst() throws LexicalException {
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.FLOAT);

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //literal ::= "{" literal-rept "}"
    private void procLiteral() throws LexicalException {
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.STRING);

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //identifier ::= letter-under identifier-tail
    private void procId() throws LexicalException {
        System.out.println("Entering:"+new Object(){}.getClass().getEnclosingMethod().getName());

        eat(TokenType.ID);

        System.out.println("Exiting:"+new Object(){}.getClass().getEnclosingMethod().getName());
    }

    //letter-digit ::= letter | digit (sem proc)
    //letter-under ::= letter | _ (sem proc)
    //letter ::= [A-za-z] (sem proc)
    //digit ::= [0-9] (sem proc)
    //caractere ::= um dos caracteres ASCII, exceto quebra de linha (sem proc)
}
