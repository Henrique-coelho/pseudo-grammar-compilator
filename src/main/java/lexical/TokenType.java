package lexical;

public enum TokenType {
    // SPECIALS
    UNEXPECTED_EOF,
    INVALID_TOKEN,
    END_OF_FILE,

    // SYMBOLS
    OPEN_BRA,      // "("
    CLOSE_BRA,     // ")"
    OPEN_PAR,      // "{"
    CLOSE_PAR,     // "}"
    SEMICOLON,     // ";"
    COMMA,         // ","
    ASSIGN,        // "="
    DOT,           // "."
    EXCLAMATION,   // "!"

    // OPERATORS
    EQUALS,        // ==
    NOT_EQUALS,    // <>
    GREATER,       // >
    GREATER_EQ,    // >=
    LOWER,         // <
    LOWER_EQ,      // <=

    ADD,           // +
    SUB,           // -
    OR,            // ||

    MUL,           // *
    DIV,           // /
    AND,           // &&

    // KEYWORDS
    START,         // start
    EXIT,          // exit
    IF,            // if
    THEN,          // then
    ELSE,          // else
    END,           // end
    DO,            // do
    WHILE,         // while
    SCAN,          // scan
    PRINT,         // print
    INTEGER_KW,    // integer keyword
    FLOAT_KW,      // float keyword
    STRING_KW,     // string keyword

    // OTHERS
    ID,            // identifier
    INTEGER,       // integer
    FLOAT,         // float
    STRING         // string

};