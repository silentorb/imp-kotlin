package silentorb.imp.parsing.lexer

val identifierStart = patternFromRegex("[a-zA-Z_]")
val identifierAfterStart = patternFromRegex("[a-zA-Z_0-9]")

val integerStart = patternFromRegex("[1-9]")
val integerAfterStart = patternFromRegex("[0-9]")

val floatAfterDot = integerAfterStart

val singleLineWhitespace = patternFromRegex("[ \\t]")

const val dot = '.'

val floatingPointSuffix = 'f'
