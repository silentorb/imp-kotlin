package silentorb.imp.parsing.syntax

import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.lexer.Rune

fun isAssignment(token: Token) =
    token.rune == Rune.assignment

fun isComma(token: Token) =
    token.rune == Rune.comma

fun isDot(token: Token) =
    token.rune == Rune.dot

fun isFloat(token: Token) =
    token.rune == Rune.literalFloat

fun isInteger(token: Token) =
    token.rune == Rune.literalInteger

fun isParenthesesClose(token: Token) =
    token.rune == Rune.parenthesesClose

fun isParenthesesOpen(token: Token) =
    token.rune == Rune.parenthesesOpen

fun isImport(token: Token) =
    token.rune == Rune.identifier && token.value == "import"

fun isLet(token: Token) =
    token.rune == Rune.identifier && token.value == "let"

fun isIdentifier(token: Token) =
    token.rune == Rune.identifier

fun isOperator(token: Token) =
    token.rune == Rune.operator

fun isNewline(token: Token) =
    token.rune == Rune.newline

fun isEndOfFile(token: Token) =
    token.rune == Rune.eof

fun isWildcard(token: Token) =
    token.rune == Rune.operator && token.value == "*"
