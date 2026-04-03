grammar FaaSChalCore;

file: procedure* ;

choreography: interaction (';' choreography)?
            | terminated
            ;

terminated: ZERO ;
role: ID ;
media: ID ;
variable: ID ;
label: ID ;

interaction: communication
          | selection
          | assignment
          | request
          | requestResponse
          | end
          | endResponse
          | conditional
          ;

communication: expression '@' role '->' expression '@' role ;

selection: role '->' role '[' label ']' ;

assignment: variable '@' role '=' expression '@' role ;
expression: CONSTANT
          | variable
          | function
          ;
function: ID '(' functionParameters? ')' ;
functionParameters: expression (',' expression)* ;

request: expression '@' role '-' media '->' variable '@' role ;

requestResponse: expression '@' role '<-' media '->' variable '@' role '|>' variable '@' role ;

end: 'end' role ;

endResponse: 'end' expression '@' role '->' role ;

conditional: 'if' expression '@' role 'then' '{' choreography '}' 'else' '{' choreography '}' ;

procedure: 'def' ID '('  ')' '{' choreography '}';


ZERO : '0' ;
WS : [ \t\r\n]+ -> skip ;
CONSTANT : [0-9]+
         | STRING
         ;
STRING : '"' ('""'|~'"')* '"' ;
ID: [a-zA-Z]+ [0-9]* ; //catch variable, process e label
