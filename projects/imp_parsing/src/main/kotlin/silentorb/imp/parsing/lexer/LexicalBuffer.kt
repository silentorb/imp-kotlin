package silentorb.imp.parsing.lexer

// TODO: When performance matters, using a String here is very inefficient but also very isolated and easy to replace
typealias LexicalBuffer = String

fun newLexicalBuffer(): LexicalBuffer = ""

fun newLexicalBuffer(character: Char): LexicalBuffer = character.toString()

fun lexicalBufferToString(buffer: LexicalBuffer): String =
    buffer

fun lexicalBufferLength(buffer: LexicalBuffer): Int =
    buffer.length

fun appendToLexicalBuffer(buffer: LexicalBuffer, character: Char): LexicalBuffer =
    buffer + character
