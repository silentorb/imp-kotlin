package silentorb.imp.parsing.syntax

import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.lexer.Rune

fun isAssignment(token: Token) =
    token.rune == Rune.assignment

fun isBraceClose(token: Token) =
    token.rune == Rune.braceClose

fun isBraceOpen(token: Token) =
    token.rune == Rune.braceOpen

fun isColon(token: Token) =
    token.rune == Rune.colon

fun isComma(token: Token) =
    token.rune == Rune.comma

fun isDot(token: Token) =
    token.rune == Rune.dot

fun isFloat(token: Token) =
    token.rune == Rune.literalFloat

fun isInteger(token: Token) =
    token.rune == Rune.literalInteger

fun isImport(token: Token) =
    token.rune == Rune.identifier && token.value == "import"

fun isLet(token: Token) =
    token.rune == Rune.identifier && token.value == "let"

fun isParenthesesClose(token: Token) =
    token.rune == Rune.parenthesesClose

fun isParenthesesOpen(token: Token) =
    token.rune == Rune.parenthesesOpen

fun isIdentifier(token: Token) =
    token.rune == Rune.identifier

fun isNewline(token: Token) =
    token.rune == Rune.newline

fun isOperator(token: Token) =
    token.rune == Rune.operator

fun isEndOfFile(token: Token) =
    token.rune == Rune.eof

fun isWildcard(token: Token) =
    token.rune == Rune.operator && token.value == "*"
