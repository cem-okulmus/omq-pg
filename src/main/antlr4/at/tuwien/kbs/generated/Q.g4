grammar Q ;

query : head ':-' body EOF ;

head : 'q(' (variable (',' variable)*)? ')' ;

variable : WORD ;

body : atom (',' atom )* ;

atom : concepts | roles | arbitraryLengthRoles;

concepts : conceptnames '(' variable ')' ;

conceptnames : conceptname | '(' conceptname ('|' conceptname)+ ')' ;

conceptname : words ;

roles : properties '(' left=variable ',' right=variable ')' ;

arbitraryLengthRoles : rolenames '(' left=variable ',' right=variable ')' ;

rolenames : rolename '*' | '(' rolename ('|' rolename)+ ')' '*';

properties : property | '(' property ('|' property)+ ')' ;

property : rolename | inverse ;

rolename : words ;

inverse : words'-' ;

words : WORD ('_' WORD)* ;

WORD : LETTER+ ;

fragment LETTER : ('a'..'z' | 'A'..'Z') ;

UNKNOWN_CHAR : . ;