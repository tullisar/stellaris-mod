lexer grammar ParadoxClausewitzLexer;

tokens {
	WHITESPACE,
	COMMENT,
	EQUALS,
	NOT_EQUALS,
	MINUS,
	GREATER_THAN,
	GREATER_EQUAL_THAN,
	LESS_THAN,
	LESS_EQUAL_THAN,
	LEFT_BRACE,
	RIGHT_BRACE,
	COLON,
	DOT,
	AT,
	YES,
	NO,
	COLOR_TYPE,
	ARITHMETIC_OPERATOR,
	RELATIONAL_OPERATOR,
	LOCALIZER_COMMAND_BEGIN,
	LOCALIZER_COMMAND_END,
	MATH_COMMAND_BEGIN,
	MATH_COMMAND_END,
	MACRO_BEGIN,
	MACRO_CONTENTS,
	MACRO_END,
	STRING_BEGIN,
	STRING_VALUE,
	STRING_END,
	PARENTHESIS,
	BRACKET,
	IDENTIFIER,
	IDENTIFIER_CONTINUATION,
	INVALID_TOKEN,
	EVENT_ID,
	NUMBER,
	EVENT_TARGET,
	GLOBAL_EVENT_TARGET,
	TRIGGER,
	RANDOM_LIST,
	SAVED_SCOPE,
	SIGN
}

channels {
	COMMENT_CHANNEL
}

/* -------------------------------------------------------------------- */
/*                          LEXER GRAMMAR                               */
/* -------------------------------------------------------------------- */
fragment A: [aA]; // match either an 'a' or 'A'
fragment B: [bB];
fragment C: [cC];
fragment D: [dD];
fragment E: [eE];
fragment F: [fF];
fragment G: [gG];
fragment H: [hH];
fragment I: [iI];
fragment J: [jJ];
fragment K: [kK];
fragment L: [lL];
fragment M: [mM];
fragment N: [nN];
fragment O: [oO];
fragment P: [pP];
fragment Q: [qQ];
fragment R: [rR];
fragment S: [sS];
fragment T: [tT];
fragment U: [uU];
fragment V: [vV];
fragment W: [wW];
fragment X: [xX];
fragment Y: [yY];
fragment Z: [zZ];
fragment ASCII_ALPHA: [a-zA-Z];
fragment ASCII_DIGIT: [0-9];
fragment OPERATOR_ADD: '+';
fragment OPERATOR_SUBTRACT: '-';
fragment OPERATOR_DIVIDE: '/';
fragment OPERATOR_MULTIPLY: '*';
fragment BACKSLASH: '\\';
fragment DOUBLE_QUOTE: '"';
fragment NEWLINE: '\r\n' | [\r\n\u2028\u2029];
fragment LEFT_PARENS: '(';
fragment RIGHT_PARENS: ')';
fragment LEFT_BRACKET: '[';
fragment RIGHT_BRACKET: ']';
fragment DOLLAR: '$';
fragment UNDERSCORE: '_';
fragment PIPE: '|';

// Whitespace is defined as the following set of characters, and are sent to the hidden channel
fragment WHITESPACE_SET: [ \t\n\r\u00A0\uFEFF\u2003];
WHITESPACE: WHITESPACE_SET+ -> channel(HIDDEN);

// Comments are only allowed in the default mode
COMMENT: '#' .*? (NEWLINE | EOF) -> channel(COMMENT_CHANNEL);

// Operator tokens
NOT_EQUALS: '!=';
EQUALS: '=';
GREATER_EQUAL_THAN: '>=';
LESS_EQUAL_THAN: '<=';
GREATER_THAN: '>';
LESS_THAN: '<';
LEFT_BRACE: '{';
RIGHT_BRACE: '}';
COLON: ':';
DOT: '.';
AT: '@';

// Boolean literals
NO: N O;
YES: Y E S;

// Color type literals
COLOR_TYPE: 'hsv' | 'rgb';

// Event target references are special tokens on their own
EVENT_TARGET: 'event_target';
GLOBAL_EVENT_TARGET: 'global_event_target';

// Special property keywords that are reserved
TRIGGER: T R I G G E R;
RANDOM_LIST: R A N D O M UNDERSCORE L I S T;

// In the default mode, the set of allowed tokens is fairly restrictive. Identifiers are the most
// common type of token, and are represented by a sequence of valid ASCII letters, digits, and
// underscores. They must begin with an ASCII letter. The special case of a single character
// identifier token followed by a newline/whitespace/EOF character is treated as an invalid token.
fragment IDENTIFIER_ALLOWED: ( ASCII_ALPHA | ASCII_DIGIT | '_');
IDENTIFIER_SINGLE: ASCII_ALPHA (NEWLINE | WHITESPACE_SET | EOF) -> type(INVALID_TOKEN);
IDENTIFIER_START:
	ASCII_ALPHA IDENTIFIER_ALLOWED* -> type(IDENTIFIER), pushMode(IN_IDENTIFIER);

// Macros can be found in most modes
MACRO_BEGIN: DOLLAR -> pushMode(MACRO_EXPANSION);

// Numbers can be integers or decimals. Outside of inline math, the only arithmetic operator is
// the unary minus (negative) operator. Leading '+' are not allowed for numeric literals.
fragment INT: '0' | ASCII_DIGIT ASCII_DIGIT*;
NUMBER: (INT (DOT ASCII_DIGIT*)?) | (DOT ASCII_DIGIT+);
SIGN: OPERATOR_SUBTRACT;
EVENT_ID: INT+;

// Encountering the '@\[' sequence enters inline math mode 
MATH_COMMAND_BEGIN: AT BACKSLASH LEFT_BRACKET -> pushMode(INLINE_MATH);

// In DEFAULT_MODE, encountering a double quote pushes the lexer into IN_STRING mode, where the set
// of tokenized characters is different.
STRING_BEGIN: DOUBLE_QUOTE -> pushMode(IN_STRING);

// -----------------------------------------------------------------------------------------------
// Mode: IN_IDENTIFIER
// 
// Identifiers in scripts are used for a lot of things. The general form of a regular expression for
// capturing an identifier in well-behaved source files is:
// ________________________________________________________________
// /(?<identifier>[a-zA-Z][\w\d]+)/gm
// 
// Though in practice identifiers are found in a number of places, and can be mixed with macro
// expansions. This mode helps to delegate the possibilities.
// -----------------------------------------------------------------------------------------------
mode IN_IDENTIFIER;

// Any newline or whitespace ends the identifier, and pops the mode. Encountering the end of file
// sets the mode back to the DEFAULT_MODE for consistency.
IDENTIFIER_WHITESPACE_EXIT: (NEWLINE | WHITESPACE) -> type(WHITESPACE), channel(HIDDEN), popMode;
IDENTIFIER_EOF_EXIT: EOF -> type(INVALID_TOKEN), mode(DEFAULT_MODE);

// In this mode, the first most likely thing is that a macro expansion has been encountered if
// there is no whitespace. 
IDENTIFIER_MACRO_START: MACRO_BEGIN -> type(MACRO_BEGIN), pushMode(MACRO_EXPANSION);

// Otherwise, additional characters means that macro mode has been popped and that there are
// more characters left in the current identifier. Tokenize these together. 
IDENTIFIER_ADDITIONAL_SEQUENCE: IDENTIFIER_ALLOWED+ -> type(IDENTIFIER_CONTINUATION);

// The following operators need to be captured while in identifier mode in the event there is
// no whitespace separating a key from an operator. Encountering these also exits identifier
// mode.
IDENTIFIER_DOT: DOT -> type(DOT), popMode;
IDENTIFIER_NOT_EQUALS: NOT_EQUALS -> type(NOT_EQUALS), popMode;
IDENTIFIER_EQUALS: EQUALS -> type(EQUALS), popMode;
IDENTIFIER_GREATER_EQUAL_THAN: GREATER_EQUAL_THAN -> type(GREATER_EQUAL_THAN), popMode;
IDENTIFIER_LESS_EQUAL_THAN: LESS_EQUAL_THAN -> type(LESS_EQUAL_THAN), popMode;
IDENTIFIER_GREATER_THAN: GREATER_THAN -> type(GREATER_THAN), popMode;
IDENTIFIER_LESS_THAN: LESS_THAN -> type(LESS_THAN), popMode;
IDENTIFIER_RIGHT_BRACE: RIGHT_BRACE -> type(RIGHT_BRACE), popMode;
// IDENTIFIER_LEFT_BRACE: LEFT_BRACE -> type(LEFT_BRACE), popMode;

// @ characters encountered within an identifier generally indicate a dynamic flag assignment
// (such as is_friend_of_@ROOT). The @ will be tokenized, but the lexer remains in identifier
// mode. 
IDENTIFIER_AT: AT -> type(AT), pushMode(IN_SAVED_SCOPE);

// Any other character encountered is treated as an invalid token of it's own, and results in
// the mode being popped.
IDENTIFIER_INVALID: . -> type(INVALID_TOKEN), popMode;

// -----------------------------------------------------------------------------------------------
// Mode: IN_SAVED_SCOPE
// -----------------------------------------------------------------------------------------------
mode IN_SAVED_SCOPE;
SAVED_SCOPE_WHITESPACE_EXIT: (NEWLINE | WHITESPACE) -> type(WHITESPACE), channel(HIDDEN), popMode, popMode;
SAVED_SCOPE_EOF_ABORT: EOF -> type(INVALID_TOKEN), mode(DEFAULT_MODE);

// Event target references are special tokens on their own
SAVED_EVENT_TARGET: EVENT_TARGET -> type(EVENT_TARGET);
SAVED_GLOBAL_EVENT_TARGET: GLOBAL_EVENT_TARGET -> type(GLOBAL_EVENT_TARGET);

SAVED_SCOPE_COLON: COLON -> type(COLON);

SCOPE_IDENTIFIER: IDENTIFIER_ALLOWED+ -> type(IDENTIFIER);
SCOPE_DOT: DOT -> type(DOT);

SCOPE_INVALID: . -> type(INVALID_TOKEN), popMode, popMode;

// -----------------------------------------------------------------------------------------------
// Mode: IN_STRING
// -----------------------------------------------------------------------------------------------

// Within the contexty of IN_STRING mode, just about every character is valid with the exception of
// some control characters. An unescaped double quote character will exit the string mode.
// Otherwise, various character seqeuences are tokenized as appropriate. A string literal contains
// zero to may tokens.
mode IN_STRING;

// Encountering a new line or end of file will result in the token being treated as invalid, and
// the default mode being set. Encountering another double quote character in this mode will exit
// the string mode.
STRING_ABORT: (NEWLINE | EOF) -> type(INVALID_TOKEN), mode(DEFAULT_MODE);
STRING_END: DOUBLE_QUOTE -> popMode;

// Each sequence of valid characters is grouped into a single token. Special cases are made for
// several escape sequences to eliminate false positives on inline commands, and to improve performance
// on the common escape character sequences.
fragment NOT_INLINE_COMMANDS: ('\\\\[[' | '@\\[[' | '[[' | '\\t' | '\\n');
fragment CHAR: ~["$\\\r\n\u2028\u2029];
CHAR_SEQUENCE: (CHAR | NOT_INLINE_COMMANDS)+ -> type(STRING_VALUE);

// Inline localizers require a specific escape sequence to be valid. Tokenize the escape sequence as
// its own token, so the contents can be parsed separately.
INLINE_LOC_START:
	'\\\\[' -> type(LOCALIZER_COMMAND_BEGIN), pushMode(INLINE_LOC);

// Inline math statements within a string appear just as they do outside of a string, so the global
// mode is used.
STRING_INLINE_MATH_START:
	MATH_COMMAND_BEGIN -> type(MATH_COMMAND_BEGIN), pushMode(INLINE_MATH);

// Other escape seqeuences are allowed, typically just for \n and \t, but technically ANY character
// may follow. The backslash and the captured character are treated as a single token of type
// ESCAPE_CHAR. However, new line and end of file result in the default mode as with other
// mode abort methods.
ESCAPE_START: '\\' -> more, pushMode(IN_STRING_ESCAPE);
mode IN_STRING_ESCAPE;
ESCAPE_ABORT: (NEWLINE | EOF) -> type(INVALID_TOKEN), mode(DEFAULT_MODE);
ESCAPE_CHAR: . -> popMode;

// -----------------------------------------------------------------------------------------------
// Mode: INLINE_LOC
// -----------------------------------------------------------------------------------------------

// Within the context of an inline localization call, the set of allowed characters is different.
// Either type of bracket will exit the inline localizer mode. Otherwise, the identifiers of the
// inline localizer are emitted as individual tokens. Invalid characters within a localizer are
// consumed and treated as a single token.
mode INLINE_LOC;
LOC_ABORT: (NEWLINE | EOF) -> type(INVALID_TOKEN), mode(DEFAULT_MODE);
LOCALIZER_COMMAND_END: RIGHT_BRACKET -> popMode, type(LOCALIZER_COMMAND_END);

// Whitespace is tokenized and retained, while everything else encountered until the end of the
// command is tokenized either as an identifier or a variable separator.
LOC_WHITESPACE: WHITESPACE -> type(WHITESPACE), channel(HIDDEN);
LOC_SEPARATOR: DOT -> type(DOT);
LOC_IDENTIFIER: IDENTIFIER_ALLOWED+ -> type(IDENTIFIER);
LOC_INVALID: . -> type(INVALID_TOKEN);

// TODO(tullisar): Support macro expansions within inline localizer commands.

// -----------------------------------------------------------------------------------------------
// Mode: INLINE_MATH
// -----------------------------------------------------------------------------------------------

// Within the context of inline math, 
mode INLINE_MATH;
MATH_ABORT: (NEWLINE | EOF | LEFT_BRACKET) -> type(INVALID_TOKEN), mode (DEFAULT_MODE);
MATH_COMMAND_END: RIGHT_BRACKET -> popMode;
MATH_PARENS: (LEFT_PARENS | RIGHT_PARENS) -> type(PARENTHESIS);
MATH_OPERATORS: (
		OPERATOR_ADD
		| OPERATOR_SUBTRACT
		| OPERATOR_MULTIPLY
		| OPERATOR_DIVIDE
		| PIPE
	) -> type(ARITHMETIC_OPERATOR);
MATH_NUMBER: NUMBER -> type(NUMBER);
MATH_MACRO_BEGIN: MACRO_BEGIN -> type(MACRO_BEGIN), pushMode(MACRO_EXPANSION);
MATH_WHITESPACE: WHITESPACE -> type(WHITESPACE), channel(HIDDEN);
MATH_INVALID: . -> type(INVALID_TOKEN);

// -----------------------------------------------------------------------------------------------
// Mode: MACRO_EXPANSION
// 
// Macro expansions are expressions enclosed by a pair of '$' characters. The general form of a
// regular expression for capturing a macro expansion in well-behaved source files is:
// ________________________________________________________________
// /[$](IDENTIFIER)(?:[|](?<alternate>(NUMBER|IDENTIFIER)))?[$]/gm
// 
// Where IDENTIFIER is the expression for capturing a valid identifier (as above), and NUMBER is the
// expression for capturing a number above.
// 
// However, to allow the parser more freedom to capture issues, macros are tokenized a little more
// freely. A '$' character in many scopes will enter this mode (and be emitted as a token). While in
// this mode, another '$' character will result in the mode returning to the previous mode. Any
// newline encountered will cause set the lexer back to the DEFAULT_MODE. Whitespace characters are
// tokenized as a group, as are sequences of all other characters.
// -----------------------------------------------------------------------------------------------
mode MACRO_EXPANSION;
MACRO_END: DOLLAR -> popMode;
MACRO_ABORT: NEWLINE -> mode(DEFAULT_MODE);
MACRO_WHITESPACE: WHITESPACE -> type(WHITESPACE);
fragment MACRO_ALLOWED: (IDENTIFIER_ALLOWED | PIPE | AT | DOT); 
MACRO_CONTENTS: MACRO_ALLOWED+;
MACRO_INVALID: . -> type(INVALID_TOKEN);

// TODO(tullisar): For now, everythign within a $$ pair is treated just as "MACRO CONTENTS".