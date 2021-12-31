parser grammar ParadoxClausewitzParser;

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
	BOOLEAN,
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
	INVALID_TOKEN,
	EVENT_ID,
	NUMBER,
	EVENT_TARGET,
	GLOBAL_EVENT_TARGET,
	SIGN
}

/* -------------------------------------------------------------------- */
/*                         PARSER GRAMMAR                               */
/* -------------------------------------------------------------------- */

// Modules are made of one or more elements
module: element*;

// Each element is like an element in a map, there is a key and an associated value, which is an
// object.
element: key EQUALS object;

// Keys are composed of valid identifier tokens
key: (identifier | numberLiteral);

// Each object is similar to another dictionary, containing a set of keys and values, where the
// values can be objects as well as other types.
object: map;
map: blockBegin (property | comparison)+ blockEnd;
blockBegin: LEFT_BRACE;
blockEnd: RIGHT_BRACE;
property: key EQUALS value;
comparison:
	key relationalOperator (
		numberLiteral
		| objectReference
		| variableReference
		| constantReference
		| macroExpansion
	);
relationalOperator: (
		EQUALS
		| NOT_EQUALS
		| GREATER_EQUAL_THAN
		| LESS_EQUAL_THAN
		| GREATER_THAN
		| LESS_THAN
	);

// Values can be one of several other types
value: reference | literal | container | color;

// A reference is a value which refers to another object by name/key
reference:
	eventReference
	| objectReference
	| variableReference
	| constantReference
	| macroExpansion;

// Macro Expansions
macroExpansion: MACRO_BEGIN MACRO_CONTENTS MACRO_END;

// Identifiers
identifier: (IDENTIFIER macroExpansion?)+;

// Event references refer to an event in another namespace TODO(tullisar): Allow event ID to be
// parameterized
eventReference: identifier variableSeparator eventId;
eventId: (NUMBER | EVENT_ID);

// Object references are by identifier
objectReference: identifier;

// Variable references are also by identifier, but can contain dots for scoping
targetResolution: COLON;
variableReference: (
		(EVENT_TARGET | GLOBAL_EVENT_TARGET) targetResolution
	)? identifier;

// Constant references are similar to object references by identifier with a leading AT
constantReference: AT identifier;

// Inline math reference
inlineMathReference:
	MATH_COMMAND_BEGIN (
		PARENTHESIS
		| numberLiteral
		| arithmeticOperator
		| variableReference
		| constantReference
	)*? MATH_COMMAND_END;
arithmeticOperator: ARITHMETIC_OPERATOR;

// Variable separators
variableSeparator: DOT;

// Literal values are also supported
literal: booleanLiteral | numberLiteral | stringLiteral;

numberLiteral: SIGN? NUMBER;
booleanLiteral: BOOLEAN;
stringLiteral:
	STRING_BEGIN (
		STRING_VALUE
		| inlineLocalizerCommand
		| inlineMathReference
	)* STRING_END;

// Inline localizer command
inlineLocalizerCommand:
	LOCALIZER_COMMAND_BEGIN (IDENTIFIER (DOT IDENTIFIER)*) LOCALIZER_COMMAND_END;

// Containers are values which are enclosed in braces, such as object definitions and collections.
container: emptyContainer | object | collection;

// Collections are a type of value which represent a group of values
collection: LEFT_BRACE collectionValue+ LEFT_BRACE;
collectionValue: (reference | literal);

// Colors are special.
color:
	colorType LEFT_BRACE colorComponent colorComponent colorComponent LEFT_BRACE;
colorType: COLOR_TYPE;
colorComponent: NUMBER;

// Empty container
emptyContainer: LEFT_BRACE RIGHT_BRACE;
