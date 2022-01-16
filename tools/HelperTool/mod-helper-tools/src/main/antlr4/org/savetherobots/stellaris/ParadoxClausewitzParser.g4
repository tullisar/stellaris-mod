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

/* -------------------------------------------------------------------- */
/*                         PARSER GRAMMAR                               */
/* -------------------------------------------------------------------- */

// Modules are made of one or more elements or random lists. Random lists are special top level
// elements that do not have an identifier as their key.
module: (constantDeclaration | randomListDeclaration | objectDeclaration)*;
equals: EQUALS;
key: IDENTIFIER;
randomListDeclaration: RANDOM_LIST equals randomList;
objectDeclaration: key equals objectDefinition;
constantDeclaration: AT identifier equals numberLiteral;

// Each object is similar to another dictionary, containing a set of keys and values, where the
// values can be objects as well as other types.
randomList: objectDefinition;
objectDefinition: map;
mapEntry: (property | comparison);
map: blockBegin mapEntry+ blockEnd;
blockBegin: LEFT_BRACE;
blockEnd: RIGHT_BRACE;
propertyKey: (
		(eventTarget? identifier (DOT identifier)*)
		| numberLiteral
		| TRIGGER
	);
property: propertyKey equals value;
comparison:
	propertyKey relationalOperator (
		numberLiteral
		| objectReference
		| variableReference
		| constantReference
		| macroExpansion
		| objectDefinition
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
	| flagReference
	| variableReference
	| constantReference
	| inlineMathReference
	| macroExpansion;

// Macro Expansions
macroExpansion: MACRO_BEGIN MACRO_CONTENTS MACRO_END;

// Identifiers
identifier:
	IDENTIFIER (macroExpansion IDENTIFIER_CONTINUATION?)*;

// Event references refer to an event in another namespace TODO(tullisar): Allow event ID to be
// parameterized
eventReference:
	identifier (variableSeparator identifier)* variableSeparator eventId;
eventId: (NUMBER | EVENT_ID);

// Object references are by identifier
objectReference: identifier;

// Variable references are also by identifier, but can contain dots for scoping
targetResolution: COLON;
eventTarget: (EVENT_TARGET | GLOBAL_EVENT_TARGET | TRIGGER) targetResolution;
variableReference:
	eventTarget? objectReference (DOT objectReference)*;

// Constant references are similar to object references by identifier with a leading AT
constantReference: AT identifier;

// Flag references are a special kind of object identifier that has a dynamic flag suffix which is a
// variable reference.
flagReference: objectReference AT variableReference;

// Inline math reference
inlineMathReference:
	MATH_COMMAND_BEGIN (
		PARENTHESIS
		| numberLiteral
		| arithmeticOperator
		| variableReference
		| constantReference
		| macroExpansion
	)*? MATH_COMMAND_END;
arithmeticOperator: ARITHMETIC_OPERATOR;

// Variable separators
variableSeparator: DOT;

// Literal values are also supported
literal: booleanLiteral | numberLiteral | stringLiteral;

numberLiteral: SIGN? NUMBER;
booleanLiteral: YES | NO;
stringContents: (
		STRING_VALUE
		| inlineLocalizerCommand
		| inlineMathReference
	)*;
stringLiteral: STRING_BEGIN stringContents STRING_END;

// Inline localizer command
inlineLocalizerCommand:
	LOCALIZER_COMMAND_BEGIN (IDENTIFIER (DOT IDENTIFIER)*) LOCALIZER_COMMAND_END;

// Containers are values which are enclosed in braces, such as object definitions and collections.
container: emptyContainer | objectDefinition | collection;

// Collections are a type of value which represent a group of values
collection: LEFT_BRACE collectionValue+ RIGHT_BRACE;
collectionValue: (reference | literal);

// Colors are special.
color: colorType collection;
colorType: COLOR_TYPE;

// Empty container
emptyContainer: LEFT_BRACE RIGHT_BRACE;
