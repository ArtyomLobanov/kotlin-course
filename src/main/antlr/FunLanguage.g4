grammar FunLanguage;

file : block;
block : (statement)*;
blockWithBraces : '{' block '}';

statement
    : functionDefinition
    | variableDeclaration
    | expression
    | whileStatement
    | ifStatement
    | assignmentStatement
    | returnStatement
    ;

functionDefinition : 'fun' IDENTIFIER '(' parameterNames ')' blockWithBraces;
variableDeclaration : 'var' IDENTIFIER ('=' expression)?;
parameterNames : (IDENTIFIER (',' IDENTIFIER)*)?;
whileStatement : 'while' '(' expression ')' blockWithBraces;
ifStatement : 'if' '(' expression ')' blockWithBraces ('else' blockWithBraces)?;
assignmentStatement : IDENTIFIER '=' expression;
returnStatement : 'return' expression;
functionCall : IDENTIFIER '(' arguments ')';
arguments : (expression (',' expression)*)?;


expression
    : binaryExpression
    | primitiveExpression
    ;

binaryExpression
    : primitiveExpression op = (MULTIPLY | DIVIDE | REMAINDER) expression
    | primitiveExpression op = (PLUS | MINUS) expression
    | primitiveExpression op = (GREATER | LESS | GREATER_OR_EQUAL | LESS_OR_EQUAL) expression
    | primitiveExpression op = (EQUAL | NOT_EQUAL) expression
    | primitiveExpression op = LOGICAL_AND expression
    | primitiveExpression op = LOGICAL_OR expression
    ;

primitiveExpression
    : functionCall
    | IDENTIFIER
    | INTEGER
    | '(' expression ')'
    ;


MULTIPLY : '*';
DIVIDE : '/';
REMAINDER : '%';
PLUS : '+';
MINUS : '-';
GREATER : '>';
LESS : '<';
GREATER_OR_EQUAL : '>=';
LESS_OR_EQUAL : '<=';
EQUAL : '==';
NOT_EQUAL : '!=';
LOGICAL_OR : '||';
LOGICAL_AND : '&&';
INTEGER : '0'
        | ('-'?[1-9][0-9]*);

IDENTIFIER : ([a-zA-Z_][a-zA-Z_0-9]*);
COMMENT : '//' ~[\r\n]* -> skip;
WS : (' ' | '\t' | '\r'| '\n') -> skip;