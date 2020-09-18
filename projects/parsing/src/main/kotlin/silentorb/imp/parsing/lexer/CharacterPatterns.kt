package silentorb.imp.parsing.lexer

val identifierStart = patternFromRegex("[a-zA-Z_]")
val identifierAfterStart = patternFromRegex("[a-zA-Z_0-9]")

val integerStart = patternFromRegex("[1-9]")
val integerAfterStart = patternFromRegex("[0-9]")

val invalidAfterZero = identifierAfterStart

val literalZero = patternFromChar('0')

val floatAfterDot = integerAfterStart

val singleLineWhitespace = patternFromRegex("[ \\t]")

const val dot = '.'
const val quoteCharacter = '"'
val newLineCharacters = listOf('\r', '\n')

val newLineStart = patternFromRegex("[\\r\\n]")
val newLineAfterStart = patternFromRegex("[\\t \\r\\n]")
val whitespace = patternFromChars(listOf(' ', '\t') + newLineCharacters)
val parentheses = patternFromChars(listOf('(', ')'))
val commentStartOrHyphen = patternFromChar('-')
val operatorStart = patternFromRegex("[*+\\/<>%$&@#!=?]")
val operatorAfterStart = patternFromRegex("[*+\\-/<>%$&@#!=?]")
val isQuoteCharacter = patternFromChar(quoteCharacter)

val floatingPointSuffix = 'f'

fun isValidCharacterAfterIdentifierOrLiteral(character: Char?): Boolean =
    character == null || !identifierAfterStart(character)
