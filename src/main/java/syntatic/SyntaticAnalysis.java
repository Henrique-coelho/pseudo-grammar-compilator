package syntatic;

import lexical.Lexeme;
import lexical.LexicalAnalysis;
import lexical.LexicalException;
import lexical.TokenType;
import semantic.TypeTable;
import semantic.SemanticException;
import semantic.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SyntaticAnalysis {

    private LexicalAnalysis lex;
    private Lexeme current;
    private TypeTable tt;
    private List<Integer> errorList;
    private int probErrorLine;

    public SyntaticAnalysis(LexicalAnalysis lex) throws LexicalException {
        this.lex = lex;
        this.tt = new TypeTable();
        this.errorList = new ArrayList<>();
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
            showLexicalOrSyntaticError();
        }
        return food.token;
    }

    private void showLexicalOrSyntaticError() {
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
        Type type = procProgram();
        eat(TokenType.END_OF_FILE);
        if(type == Type.ERROR) {
            StringBuilder sb = new StringBuilder();
            errorList.forEach(line -> {
                sb.append("\n");
                sb.append("Erro na linha "+(line));
            });
            throw new SemanticException("Erro semântico! "+sb);
        }

    }

    //program ::= start [decl-list] stmt-list exit
    private Type procProgram() throws LexicalException {
        eat(TokenType.START);

        Type type = Type.VOID;
        while (current.type == TokenType.STRING_KW || current.type == TokenType.INTEGER_KW || current.type == TokenType.FLOAT_KW){
            this.probErrorLine = this.lex.getLine();
            Type type1 = procDeclList();
            if (type1 != Type.VOID) {
                errorList.add(this.probErrorLine);
            }
            if (!(type == Type.VOID && type1 == Type.VOID)) {
                type = Type.ERROR;
            }
        }
        Type type2 = procStmtList();
        if(!(type == Type.VOID && type2 == Type.VOID))
            type = Type.ERROR;
        eat(TokenType.EXIT);
        return type;
    }

    //decl-list ::= decl {decl}
    private Type procDeclList() throws LexicalException{
        Type type = procDecl();
        while (current.type == TokenType.STRING_KW || current.type == TokenType.INTEGER_KW || current.type == TokenType.FLOAT_KW){
            Type type1 = procDecl();
            if(!(type == Type.VOID && type1 == Type.VOID))
                type = Type.ERROR;
        }
        return type;
    }
    //decl ::= type ident-list ";"
    private Type procDecl() throws LexicalException{
        Type type = procType();
        List<String> idList = procIdList();

        Type type_l = Type.VOID;
        for (String id:idList) {
            if(!tt.insert(id,type))
                type_l = Type.ERROR;
        }
        eat(TokenType.SEMICOLON);
        return type_l;
    }

    //ident-list ::= identifier {"," identifier}
    private List<String> procIdList() throws LexicalException{
        List<String> idList = new ArrayList<>();
        idList.add(procId());
        while(current.type == TokenType.COMMA){
            advance();
            idList.add(procId());
        }
        return idList;
    }

    //type ::= int | float | string
    private Type procType() throws LexicalException{
        if(current.type == TokenType.STRING_KW){
            advance();
            return Type.STRING;
        } else if(current.type == TokenType.INTEGER_KW){
            advance();
            return Type.INTEGER;
        } else if(current.type == TokenType.FLOAT_KW) {
            advance();
            return Type.FLOAT;
        }
        return Type.ERROR;
    }


    //stmt-list ::= stmt | {stmt}
    private Type procStmtList() throws LexicalException{
        this.probErrorLine = this.lex.getLine();
        Type type = procStatement();
        if (type != Type.VOID) {
            errorList.add(this.probErrorLine);
        }
        while(current.type == TokenType.ID ||
                current.type == TokenType.IF ||
                current.type == TokenType.DO ||
                current.type == TokenType.SCAN ||
                current.type == TokenType.PRINT){
            this.probErrorLine = this.lex.getLine();
            Type type1 = procStatement();
            if(type == Type.VOID && type1 == Type.VOID)
                type = Type.VOID;
            else
                type = Type.ERROR;
            if (type1 != Type.VOID) {
                errorList.add(this.probErrorLine);
            }
        }
        return type;
    }

    //stmt ::= assign-stmt ";" | if-stmt | while-stmt | read-stmt ";" | write-stmt ";"
    private Type procStatement() throws LexicalException{
        Type type;
        if(current.type == TokenType.ID) {
            type = procAssign();
            eat(TokenType.SEMICOLON);
            return type;
        } else if(current.type == TokenType.IF){
            type = procIf();
            return type;
        } else if(current.type == TokenType.DO) {
            type = procWhile();
            return type;
        } else if(current.type == TokenType.SCAN) {
            type = procRead();
            eat(TokenType.SEMICOLON);
            return type;
        } else if(current.type == TokenType.PRINT) {
            type = procWrite();
            eat(TokenType.SEMICOLON);
            return type;
        } else {
            showLexicalOrSyntaticError();
            return Type.ERROR;
        }
    }

    //assign-stmt ::= identifier "=" simple_expr
    private Type procAssign() throws LexicalException{
        String id = procId();
        eat(TokenType.ASSIGN);
        Type type = procSimpleExpr();

        if(tt.get(id) == type)
            return Type.VOID;
        else
            return Type.ERROR;
    }

    //if-stmt ::= if condition then stmt-list end | if condition then stmt-list else stmt-list end
    private Type procIf() throws LexicalException{
        eat(TokenType.IF);
        Type typec = procCond();
        eat(TokenType.THEN);
        Type type1 = procStmtList();

        if(current.type == TokenType.END){
            advance();

            if(typec == Type.INTEGER && type1 == Type.VOID)
                return Type.VOID;
            else
                return Type.ERROR;
        } else if(current.type == TokenType.ELSE){
            advance();
            Type type2 = procStmtList();
            eat(TokenType.END);

            if(typec == Type.INTEGER && type1 == Type.VOID && type2 == Type.VOID)
                return Type.VOID;
            else
                return Type.ERROR;
        } else {
            showLexicalOrSyntaticError();
            return Type.ERROR;
        }
    }

    //condition ::= expression
    private Type procCond() throws  LexicalException{
        return procExpr();
    }

    // while-stmt ::= do stmt-list stmt-sufix
    private Type procWhile() throws LexicalException{
        eat(TokenType.DO);
        Type type1 = procStmtList();
        Type type2 = procSufix();

        if(type1 == Type.VOID && type2 == Type.VOID)
            return Type.VOID;
        else
            return Type.ERROR;
    }

    //stmt-sufix ::= while condition end
    private Type procSufix() throws LexicalException{
        eat(TokenType.WHILE);
        Type type = procCond();
        eat(TokenType.END);

        if(type == Type.INTEGER)
            return Type.VOID;
        else
            return Type.ERROR;
    }

    //read-stmt ::= scan "(" identifier ")"
    private Type procRead() throws LexicalException{
        eat(TokenType.SCAN);
        eat(TokenType.OPEN_BRA);
        String id = procId();
        eat(TokenType.CLOSE_BRA);

        if(tt.contains(id))
            return Type.VOID;
        else
            return Type.ERROR;
    }

    //write-stmt ::= print "(" writable ")"
    private Type procWrite() throws LexicalException{
        eat(TokenType.PRINT);
        eat(TokenType.OPEN_BRA);
        Type type = procWritable();
        if(!(type==Type.ERROR))
            type = Type.VOID;
        eat(TokenType.CLOSE_BRA);
        return type;
    }

    //writable ::= simple-expr | literal
    private Type procWritable() throws LexicalException{
        if(current.type == TokenType.STRING){
            return procLiteral();
        } else {
            return procSimpleExpr();
        }
    }

    //expression ::= simple-expr | simple-expr relop simple-expr
    private Type procExpr() throws LexicalException{
        Type type1 = procSimpleExpr();
        while(current.type == TokenType.EQUALS ||
                current.type == TokenType.GREATER ||
                current.type == TokenType.GREATER_EQ ||
                current.type == TokenType.LOWER ||
                current.type == TokenType.LOWER_EQ ||
                current.type == TokenType.NOT_EQUALS){
            String op = procRelOp();
            Type type2 = procSimpleExpr();
            if ("==".equals(op)) {
                if(((type1==Type.INTEGER || type1==Type.FLOAT) && (type2==Type.INTEGER || type2==Type.FLOAT)) || (type1==Type.STRING && type2==Type.STRING))
                    type1 = Type.INTEGER;
                else
                    type1 = Type.ERROR;
            } else {
                if((type1==Type.INTEGER || type1==Type.FLOAT) && (type2==Type.INTEGER || type2==Type.FLOAT))
                    type1 = Type.INTEGER;
                else
                    type1 = Type.ERROR;
            }

        }
        return type1;
    }

    //simple-expr ::= term | simple-expr addop term
    private Type procSimpleExpr() throws LexicalException{
        Type type1 = procTerm();
        while(current.type == TokenType.ADD || current.type == TokenType.SUB || current.type == TokenType.OR){
            String op = procAddOp();
            Type type2 = procSimpleExpr();
            if(op.equals("||")){
                if(type1==Type.INTEGER && type2==Type.INTEGER)
                    type1 = Type.INTEGER;
                else
                    type1 = Type.ERROR;

            } else if(op.equals("+")){
                if((type1==Type.INTEGER || type1==Type.FLOAT) && (type2==Type.INTEGER || type2==Type.FLOAT)){
                    if(type1==Type.INTEGER && type2==Type.INTEGER)
                        type1 = Type.INTEGER;
                    else
                        type1 = Type.FLOAT;
                }
                else if(type1==Type.STRING && type2==Type.STRING){
                    type1 = Type.STRING;
                }
                else
                    type1 = Type.ERROR;
            } else if(op.equals("-")){
                if((type1==Type.INTEGER || type1==Type.FLOAT) && (type2==Type.INTEGER || type2==Type.FLOAT)){
                    if(type1==Type.INTEGER && type2==Type.INTEGER)
                        type1 = Type.INTEGER;
                    else
                        type1 = Type.FLOAT;
                }
                else
                    type1 = Type.ERROR;
            }
        }
        return type1;
    }

    //term ::= factor-a | term mulop factor-a
    private Type procTerm() throws LexicalException{
        Type type1 = procFactorAct();
        while(current.type == TokenType.MUL || current.type == TokenType.DIV || current.type == TokenType.AND){
            String op = procMulop();
            Type type2 = procTerm();
            if("&&".equals(op)){
                if(type1==Type.INTEGER && type2==Type.INTEGER)
                    type1 = Type.INTEGER;
                else
                    type1 = Type.ERROR;
            } else if("*".equals(op)){
                if((type1==Type.INTEGER || type1==Type.FLOAT) && (type2==Type.INTEGER || type2==Type.FLOAT)){
                    if(type1==Type.INTEGER && type2==Type.INTEGER)
                        type1 = Type.INTEGER;
                    else
                        type1 = Type.FLOAT;
                }
                else
                    type1 = Type.ERROR;
            } else if("/".equals(op)){
                if((type1==Type.INTEGER || type1==Type.FLOAT) && (type2==Type.INTEGER || type2==Type.FLOAT)){
                    if(type1==Type.INTEGER && type2==Type.INTEGER)
                        type1 = Type.INTEGER;
                    else
                        type1 = Type.FLOAT;
                }
                else
                    type1 = Type.ERROR;
            }
        }
        return type1;
    }

    //fator-a ::= factor | "!" factor | "-" factor
    private Type procFactorAct() throws  LexicalException{
        if(current.type == TokenType.ID || current.type == TokenType.INTEGER || current.type == TokenType.FLOAT || current.type == TokenType.STRING || current.type == TokenType.OPEN_BRA){
            return procFactor();
        } else if(current.type==TokenType.EXCLAMATION){
            eat(TokenType.EXCLAMATION);
            Type type = procFactor();
            if(type == Type.INTEGER)
                return Type.INTEGER;
            else
                return Type.ERROR;
        } else if(current.type==TokenType.SUB){
            eat(TokenType.SUB);
            Type type = procFactor();
            if(type == Type.INTEGER)
                return Type.INTEGER;
            else if(type == Type.FLOAT)
                return Type.FLOAT;
            else
                return Type.ERROR;
        } else {
            showLexicalOrSyntaticError();
            return null;
        }
    }

    //factor ::= identifier | constant | "(" expression ")"
    private Type procFactor() throws LexicalException{
        if(current.type == TokenType.ID){
            String id = procId();
            return tt.get(id);
        } else if(current.type == TokenType.INTEGER || current.type == TokenType.FLOAT || current.type == TokenType.STRING){
            return procConstant();
        } else if(current.type == TokenType.OPEN_BRA){
            eat(TokenType.OPEN_BRA);
            Type type = procExpr();
            eat(TokenType.CLOSE_BRA);
            return type;
        } else {
            showLexicalOrSyntaticError();
            return null;
        }
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
            showLexicalOrSyntaticError();
            return null;
        }
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
            showLexicalOrSyntaticError();
            return null;
        }
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
            showLexicalOrSyntaticError();
            return null;
        }
    }
    //constant ::= integer_const | float_const | literal
    private Type procConstant() throws LexicalException {
        if (current.type == TokenType.INTEGER) {
            return procIntegerConst();
        } else if (current.type == TokenType.FLOAT) {
            return procFloatConst();
        } else if (current.type == TokenType.STRING) {
            return procLiteral();
        } else {
            showLexicalOrSyntaticError();
        }
        return null;
    }
    //integer_const ::= digit integer_const_tail
    private Type procIntegerConst() throws LexicalException {
        eat(TokenType.INTEGER);
        return Type.INTEGER;
    }

    //float_const ::= integer_const “.” integer_const
    private Type procFloatConst() throws LexicalException {
        eat(TokenType.FLOAT);
        return Type.FLOAT;
    }

    //literal ::= "{" literal-rept "}"
    private Type procLiteral() throws LexicalException {
        eat(TokenType.STRING);
        return Type.STRING;
    }

    //identifier ::= letter-under identifier-tail
    private String procId() throws LexicalException {
        String id = eat(TokenType.ID);
        return id;
    }

    //letter-digit ::= letter | digit (sem proc)
    //letter-under ::= letter | _ (sem proc)
    //letter ::= [A-za-z] (sem proc)
    //digit ::= [0-9] (sem proc)
    //caractere ::= um dos caracteres ASCII, exceto quebra de linha (sem proc)
}
