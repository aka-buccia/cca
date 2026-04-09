grammar FaaSChalCore;


program: procedure* ;

procedure: 'def' procedureName '(' procedureParameters? ')' (':' terminationOrder)? '{' choreography '}';
terminationOrder: '(' orderingCouple (',' orderingCouple)? ')' ;
orderingCouple: role '<:' role ;

choreography: interaction (';' choreography)?
            | terminated
            ;

terminated: INT {Integer.parseInt($INT.text) == 0}? ;
role: ID ;
media: ID ;
variable: ID ;
constant: INT
        | STRING
        ; 
label: ID ;
procedureName: ID ;

interaction: communication
          | selection
          | assignment
          | request
          | requestResponse
          | end
          | endResponse
          | conditional
          | procedureCall
          ;

communication: expression '@' role '->' variable '@' role ;

selection: role '->' role '[' label ']' ;

assignment: variable '@' role '=' expression '@' role ;
expression: constant
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

procedureCall: procedureName '(' procedureParameters? ')';
procedureParameters: parameterList (',' parameterList)* ;
parameterList: statefulParameters
             | 'nonterm' nonterminatingParameters
             | 'term' terminatingParameters
             ;
statefulParameters: role (',' role)* ;
nonterminatingParameters: role (',' role)* ;
terminatingParameters: terminatingTerm (',' terminatingTerm)* ;
terminatingTerm: '[' role ',' role ']'
               | role
               ;


INT : [0-9]+ ;
WS : [ \t\r\n]+ -> skip ;
COMMENT: '//' ~[\r\n]+ -> skip;
STRING : '"' ('""'|~'"')* '"' ;
ID: [a-zA-Z]+ [0-9]* ; //catch variable, process e label
