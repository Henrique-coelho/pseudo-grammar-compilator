package syntatic;

import generator.Code;
import generator.addresses.*;
import lexical.Lexeme;
import lexical.LexicalAnalysis;
import lexical.LexicalException;
import lexical.TokenType;
import generator.CurrentSymbolTable;
import semantic.SemanticException;
import generator.Type;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class SyntaticAnalysis {

    private final LexicalAnalysis lex;
    private Lexeme current;

    private final CurrentSymbolTable top;
    private final Code code;

    private final List<Integer> errorList;
    private int probErrorLine;

    public SyntaticAnalysis(LexicalAnalysis lex) throws LexicalException {
        this.lex = lex;
        this.top = new CurrentSymbolTable();
        this.code = new Code();

        this.errorList = new ArrayList<>();
        this.current = lex.nextToken();
    }

    private void advance() throws LexicalException {
        current = lex.nextToken();
    }

    private String eat(TokenType type) throws LexicalException {
        Lexeme food = current;
        if (type == food.type) {
            current = lex.nextToken();
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

    public void start() throws Exception{
        Type type = procProgram();
        System.out.println(code.toString());
        eat(TokenType.END_OF_FILE);
        if(type == Type.ERROR) {
            StringBuilder sb = new StringBuilder();
            errorList.forEach(line -> sb.append("\n")
                    .append("Erro na linha ")
                    .append(line));
            throw new SemanticException("Erro semântico! "+sb);
        }
    }

    //program ::= start [decl-list] stmt-list exit
    private Type procProgram() throws Exception {
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
        Expression expr = procStmtList();
        Type type2 = expr.type();

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

        Type typeL = Type.VOID;
        for (String id:idList) {
            if(!top.put(new NameAddress(id,type)))
                typeL = Type.ERROR;
            else
                code.emit(type.toString()+" "+id);
        }
        eat(TokenType.SEMICOLON);
        return typeL;
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
    private Expression procStmtList() throws Exception{
        this.probErrorLine = this.lex.getLine();
        Expression expr = procStatement();
        Type type = Objects.requireNonNull(expr).type();


        if (type != Type.VOID) {
            errorList.add(this.probErrorLine);
        }
        while(current.type == TokenType.ID ||
                current.type == TokenType.IF ||
                current.type == TokenType.DO ||
                current.type == TokenType.SCAN ||
                current.type == TokenType.PRINT){
            this.probErrorLine = this.lex.getLine();
            Expression stmtExpr = procStatement();
            Type stmtType = stmtExpr.type();

            if(!(type == Type.VOID && stmtType == Type.VOID))
                type = Type.ERROR;
            if (stmtType != Type.VOID) {
                errorList.add(this.probErrorLine);
            }
        }
        return new Expression(type == Type.ERROR);
    }

    //stmt ::= assign-stmt ";" | if-stmt | while-stmt | read-stmt ";" | write-stmt ";"
    private Expression procStatement() throws Exception{
        Expression stmtExpr = new Expression(false);

        if(current.type == TokenType.ID) {
            stmtExpr = procAssign();
            eat(TokenType.SEMICOLON);
            return stmtExpr;
        } else if(current.type == TokenType.IF){
            stmtExpr = procIf();
            return stmtExpr;
        } else if(current.type == TokenType.DO) {
            stmtExpr = procWhile();
            return stmtExpr;
        } else if(current.type == TokenType.SCAN) {
            stmtExpr = procRead();
            eat(TokenType.SEMICOLON);
            return stmtExpr;
        } else if(current.type == TokenType.PRINT) {
            stmtExpr = procWrite();
            eat(TokenType.SEMICOLON);
            return stmtExpr;
        } else {
            showLexicalOrSyntaticError();
            return null;
        }
    }

    //assign-stmt ::= identifier "=" simple_expr
    private Expression procAssign() throws LexicalException{
        String id = procId();
        eat(TokenType.ASSIGN);
        Expression expr = procSimpleExpr();
        Type type = expr.type();

        NameAddress addr = top.get(id);
        Expression assExpr;
        if(type == addr.type()){
            code.emit(addr.value()+" = "+expr.addr());
            assExpr = new Expression(false);
        }
        else
            assExpr = new Expression(true);

        return assExpr;
    }

    //if-stmt ::= if condition then stmt-list end | if condition then stmt-list else stmt-list end
    private Expression procIf() throws Exception{
        eat(TokenType.IF);

        Expression boolExpr = procCond();
        Type boolType = boolExpr.type();

        int m1 = code.nextInst();
        code.emit("if "+boolExpr.addr()+" goto @@@","@@@");
        code.emit("goto @@@","@@@");
        eat(TokenType.THEN);

        int m2 = code.nextInst();
        Expression listExpr = procStmtList();
        Type listType = listExpr.type();

        if(current.type == TokenType.END){
            advance();

            int m3 = code.nextInst();
            Expression ifExpr = new Expression(!(boolType == Type.INTEGER && listType == Type.VOID));

            Integer[] truelist = {m1};
            Integer[] falselist = {m1+1};
            code.backpatch(Arrays.asList(truelist),m2);
            code.backpatch(Arrays.asList(falselist),m3);
            return ifExpr;
        } else {
            eat(TokenType.ELSE);

            int m3 = code.nextInst();
            code.emit("goto @@@","@@@");
            Expression elseExpr = procStmtList();
            Type elseType = elseExpr.type();
            Expression ifExpr = new Expression(!(boolType == Type.INTEGER && listType == Type.VOID && elseType == Type.VOID));

            int m4 = code.nextInst();
            eat(TokenType.END);

            Integer[] iflist = {m1};
            Integer[] elselist = {m1+1};
            Integer[] endlist = {m3};
            code.backpatch(Arrays.asList(iflist),m2);
            code.backpatch(Arrays.asList(elselist),m3+1);
            code.backpatch(Arrays.asList(endlist),m4);

            return ifExpr;
        }
    }

    //condition ::= expression
    private Expression procCond() throws  LexicalException{
        return procExpr();
    }

    // while-stmt ::= do stmt-list stmt-sufix
    private Expression procWhile() throws Exception{
        eat(TokenType.DO);

        int m1 = code.nextInst();
        Expression actExpr = procStmtList();
        Type actType = actExpr.type();

        Expression boolExpr = procSufix();
        Type boolType = boolExpr.type();
        int m2 = code.nextInst();

        Expression whileExpr = new Expression(!(actType == Type.VOID && boolType == Type.VOID));
        whileExpr.addToNextList(boolExpr.getFalselist());
        code.backpatch(boolExpr.getTruelist(),m1);
        code.backpatch(boolExpr.getFalselist(),m2);
        return whileExpr;
    }

    //stmt-sufix ::= while condition end
    private Expression procSufix() throws Exception{
        eat(TokenType.WHILE);
        Expression boolExpr = procCond();
        Type type = boolExpr.type();
        int m = code.nextInst();
        eat(TokenType.END);

        Expression whileExpr = new Expression(type != Type.INTEGER);
        int[] truelist = {m};
        int[] falselist = {m+1};
        whileExpr.addToTrueList(truelist);
        whileExpr.addToFalseList(falselist);
        code.emit("if "+boolExpr.addr()+" goto @@@","@@@");
        code.emit("goto @@@", "@@@");
        return whileExpr;
    }

    //read-stmt ::= scan "(" identifier ")"
    private Expression procRead() throws LexicalException{
        eat(TokenType.SCAN);
        eat(TokenType.OPEN_BRA);
        String id = procId();
        eat(TokenType.CLOSE_BRA);

        Expression expr;
        if(top.contains(id)){
            code.emit("scan "+id);
            expr = new Expression(false);
        }
        else
            expr = new Expression(true);
        return expr;
    }

    //write-stmt ::= print "(" writable ")"
    private Expression procWrite() throws LexicalException{
        eat(TokenType.PRINT);
        eat(TokenType.OPEN_BRA);
        Expression expr = procWritable();
        Type type = expr.type();

        if((type!=Type.ERROR)) {
            code.emit("out " + expr.addr());
            expr = new Expression(false);
        }
        else{
            expr = new Expression(true);
        }
        eat(TokenType.CLOSE_BRA);
        return expr;
    }

    //writable ::= simple-expr | literal
    private Expression procWritable() throws LexicalException{
        if(current.type == TokenType.STRING){
            return new Expression(procLiteral());
        } else {
            return procSimpleExpr();
        }
    }

    //expression ::= simple-expr | simple-expr relop simple-expr
    private Expression procExpr() throws LexicalException{
        Expression expr1 = procSimpleExpr();
        Type type1 = expr1.type();

        while(current.type == TokenType.EQUALS ||
                current.type == TokenType.GREATER ||
                current.type == TokenType.GREATER_EQ ||
                current.type == TokenType.LOWER ||
                current.type == TokenType.LOWER_EQ ||
                current.type == TokenType.NOT_EQUALS){
            String op = procRelOp();
            Expression expr2 = procSimpleExpr();
            Type type2 = expr2.type();

            Address newAddr;
            if ("==".equals(op)) {
                if(((type1==Type.INTEGER || type1==Type.FLOAT) && (type2==Type.INTEGER || type2==Type.FLOAT)) || (type1==Type.STRING && type2==Type.STRING))
                    newAddr = new TempAddress(Type.INTEGER);
                else
                    newAddr = new TempAddress(Type.ERROR);

                Expression eqExpr = new Expression(newAddr);
                code.emit(eqExpr.addr()+" = "+expr1.addr()+" == "+expr2.addr());
                expr1 = eqExpr;
                type1 = eqExpr.type();
            } else {
                if((type1==Type.INTEGER || type1==Type.FLOAT) && (type2==Type.INTEGER || type2==Type.FLOAT))
                    newAddr = new TempAddress(Type.INTEGER);
                else
                    newAddr = new TempAddress(Type.ERROR);

                Expression finalExpr = new Expression(newAddr);
                code.emit(finalExpr.addr()+" = "+expr1.addr()+" "+op+" "+expr2.addr());
                expr1 = finalExpr;
                type1 = finalExpr.type();
            }

        }
        return expr1;
    }

    //simple-expr ::= term | simple-expr addop term
    private Expression procSimpleExpr() throws LexicalException{
        Expression expr1 = procTerm();
        Type type1 = expr1.type();

        while(current.type == TokenType.ADD || current.type == TokenType.SUB || current.type == TokenType.OR){
            String op = procAddOp();
            Expression expr2 = procSimpleExpr();
            Type type2 = expr2.type();

            Address newAddr;
            switch (op) {
                case "||":
                    if (type1 == Type.INTEGER && type2 == Type.INTEGER)
                        newAddr = new TempAddress(Type.INTEGER);
                    else
                        newAddr = new TempAddress(Type.ERROR);

                    Expression orExpr = new Expression(newAddr);
                    code.emit(orExpr.addr() + " = " + expr1.addr() + " || " + expr2.addr());
                    expr1 = orExpr;
                    type1 = orExpr.type();

                    break;
                case "+":
                    if ((type1 == Type.INTEGER || type1 == Type.FLOAT) && (type2 == Type.INTEGER || type2 == Type.FLOAT)) {
                        if (type1 == Type.INTEGER && type2 == Type.INTEGER)
                            newAddr = new TempAddress(Type.INTEGER);
                        else
                            newAddr = new TempAddress(Type.FLOAT);

                        Expression addExpr = new Expression(newAddr);
                        code.emit(addExpr.addr() + " = " + expr1.addr() + " + " + expr2.addr());
                        expr1 = addExpr;
                        type1 = addExpr.type();
                    } else if (type1 == Type.STRING && type2 == Type.STRING) {
                        newAddr = new TempAddress(Type.STRING);
                        Expression addExpr = new Expression(newAddr);
                        code.emit(addExpr.addr() + " = " + expr1.addr() + " + " + expr2.addr());
                        expr1 = addExpr;
                        type1 = addExpr.type();
                    } else {
                        newAddr = new TempAddress(Type.ERROR);

                        Expression errExpr = new Expression(newAddr);
                        code.emit(errExpr.addr() + " = " + expr1.addr() + " + " + expr2.addr());
                        expr1 = errExpr;
                        type1 = errExpr.type();
                    }
                    break;
                case "-":
                    if ((type1 == Type.INTEGER || type1 == Type.FLOAT) && (type2 == Type.INTEGER || type2 == Type.FLOAT)) {
                        if (type1 == Type.INTEGER && type2 == Type.INTEGER)
                            newAddr = new TempAddress(Type.INTEGER);
                        else
                            newAddr = new TempAddress(Type.FLOAT);

                        Expression subExpr = new Expression(newAddr);
                        code.emit(subExpr.addr() + " = " + expr1.addr() + " -" + expr2.addr());
                        expr1 = subExpr;
                        type1 = subExpr.type();
                    } else {
                        newAddr = new TempAddress(Type.ERROR);

                        Expression errExpr = new Expression(newAddr);
                        code.emit(errExpr.addr() + " = " + expr1.addr() + " - " + expr2.addr());
                        expr1 = errExpr;
                        type1 = errExpr.type();
                    }
                    break;
            }
        }
        return expr1;
    }

    //term ::= factor-a | term mulop factor-a
    private Expression procTerm() throws LexicalException{
        Expression expr1 = procFactorAct();
        Type type1 = Objects.requireNonNull(expr1).type();

        while(current.type == TokenType.MUL || current.type == TokenType.DIV || current.type == TokenType.AND){
            String op = procMulop();
            Expression expr2 = procTerm();
            Type type2 = expr2.type();

            Address newAddr;
            if("&&".equals(op)){
                if(type1==Type.INTEGER && type2==Type.INTEGER)
                    newAddr = new TempAddress(Type.INTEGER);
                else
                    newAddr = new TempAddress(Type.ERROR);

                Expression andExpr = new Expression(newAddr);
                code.emit(andExpr.addr()+" = "+expr1.addr()+" && "+expr2.addr());
                expr1 = andExpr;
                type1 = andExpr.type();
            } else if("*".equals(op)){
                if((type1==Type.INTEGER || type1==Type.FLOAT) && (type2==Type.INTEGER || type2==Type.FLOAT)){
                    if(type1==Type.INTEGER && type2==Type.INTEGER)
                        newAddr = new TempAddress(Type.INTEGER);
                    else
                        newAddr = new TempAddress(Type.FLOAT);

                    Expression mulExpr = new Expression(newAddr);
                    code.emit(mulExpr.addr()+" = "+expr1.addr()+" * "+expr2.addr());
                    expr1 = mulExpr;
                    type1 = mulExpr.type();
                }
                else{
                    newAddr = new TempAddress(Type.ERROR);

                    Expression errExpr = new Expression(newAddr);
                    code.emit(errExpr.addr()+" = "+expr1.addr()+" * "+expr2.addr());
                    expr1 = errExpr;
                    type1 = errExpr.type();
                }
            } else if("/".equals(op)){
                if((type1==Type.INTEGER || type1==Type.FLOAT) && (type2==Type.INTEGER || type2==Type.FLOAT)){
                    if(type1==Type.INTEGER && type2==Type.INTEGER)
                        newAddr = new TempAddress(Type.INTEGER);
                    else
                        newAddr = new TempAddress(Type.FLOAT);

                    Expression divExpr = new Expression(newAddr);
                    code.emit(divExpr.addr()+" = "+expr1.addr()+" / "+expr2.addr());
                    expr1 = divExpr;
                    type1 = divExpr.type();
                }
                else{
                    newAddr = new TempAddress(Type.ERROR);

                    Expression errExpr = new Expression(newAddr);
                    code.emit(errExpr.addr()+" = "+expr1.addr()+" / "+expr2.addr());
                    expr1 = errExpr;
                    type1 = errExpr.type();
                }
            }
        }
        return expr1;
    }

    //fator-a ::= factor | "!" factor | "-" factor
    private Expression procFactorAct() throws  LexicalException{
        if(current.type == TokenType.ID || current.type == TokenType.INTEGER || current.type == TokenType.FLOAT || current.type == TokenType.STRING || current.type == TokenType.OPEN_BRA){
            return procFactor();
        } else if(current.type==TokenType.EXCLAMATION){
            eat(TokenType.EXCLAMATION);
            Expression expr = procFactor();
            Type type = expr.type();

            Address newAddr;
            if(type == Type.INTEGER)
                newAddr = new TempAddress(Type.INTEGER);
            else
                newAddr = new TempAddress(Type.ERROR);
            Expression notExpr = new Expression(newAddr);
            code.emit(notExpr.addr()+" = !"+expr.addr());
            return notExpr;
        } else if(current.type==TokenType.SUB){
            eat(TokenType.SUB);
            Expression expr = procFactor();
            Type type = expr.type();

            Address newAddr;
            if(type == Type.INTEGER)
                newAddr = new TempAddress(Type.INTEGER);
            else if(type == Type.FLOAT)
                newAddr = new TempAddress(Type.FLOAT);
            else
                newAddr = new TempAddress(Type.ERROR);
            Expression negExpr = new Expression(newAddr);
            code.emit(negExpr.addr()+" = -"+expr.addr());
            return negExpr;
        } else {
            showLexicalOrSyntaticError();
            return null;
        }
    }

    //factor ::= identifier | constant | "(" expression ")"
    private Expression procFactor() throws LexicalException{
        if(current.type == TokenType.ID){
            String id = procId();
            return new Expression(top.get(id));
        } else if(current.type == TokenType.INTEGER || current.type == TokenType.FLOAT || current.type == TokenType.STRING){
            return procConstant();
        } else if(current.type == TokenType.OPEN_BRA){
            eat(TokenType.OPEN_BRA);
            Expression expr = procExpr();
            eat(TokenType.CLOSE_BRA);
            return expr;
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
    private Expression procConstant() throws LexicalException {
        if (current.type == TokenType.INTEGER) {
            return new Expression(procIntegerConst());
        } else if (current.type == TokenType.FLOAT) {
            return new Expression(procFloatConst());
        } else if (current.type == TokenType.STRING) {
            return new Expression(procLiteral());
        } else {
            showLexicalOrSyntaticError();
        }
        return null;
    }

    //integer_const ::= digit integer_const_tail
    private Address procIntegerConst() throws LexicalException {
        String number = eat(TokenType.INTEGER);
        return new ConstAddress(Integer.parseInt(number));
    }

    //float_const ::= integer_const “.” integer_const
    private Address procFloatConst() throws LexicalException {
        String number = eat(TokenType.FLOAT);
        return new ConstAddress(Float.parseFloat(number));
    }

    //literal ::= "{" literal-rept "}"
    private Address procLiteral() throws LexicalException {
        String string = eat(TokenType.STRING);
        return new ConstAddress(string);
    }

    //identifier ::= letter-under identifier-tail
    private String procId() throws LexicalException {
        return eat(TokenType.ID);
    }

}
