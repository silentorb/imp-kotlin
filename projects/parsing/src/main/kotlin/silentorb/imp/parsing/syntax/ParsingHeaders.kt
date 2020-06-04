package silentorb.imp.parsing.syntax

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.lexer.Rune

fun parseHeader(token: Token): ParsingTransition =
    when {
      token.rune == Rune.identifier && token.value == "import" -> ParsingMode.importFirstPathToken to pushChild(BurgType.importKeyword)
      token.rune == Rune.identifier && token.value == "let" -> ParsingMode.definitionName to skip
      token.rune == Rune.newline -> ParsingMode.header to foldStack
      else -> parsingError(TextId.expectedImportOrLetKeywords)
    }

fun parseImportFirstPathToken(token: Token): ParsingTransition =
    when {
      token.rune == Rune.identifier -> ParsingMode.importSeparator to pushChild(BurgType.importPathToken)
      token.rune == Rune.wildcard -> ParsingMode.header to pushChild(BurgType.importPathToken)
      token.rune == Rune.newline -> null to addError(TextId.missingImportPath)
      else -> parsingError(TextId.expectedIdentifier)
    }

fun parseImportFollowingPathToken(token: Token): ParsingTransition =
    when {
      token.rune == Rune.identifier -> ParsingMode.importSeparator to addSibling(BurgType.importPathToken)
      token.rune == Rune.wildcard -> ParsingMode.header to addSibling(BurgType.importPathWildcard)
      else -> parsingError(TextId.expectedIdentifierOrWildcard)
    }

fun parseImportSeparator(token: Token): ParsingTransition =
    when {
      token.rune == Rune.dot -> ParsingMode.importFollowingPathToken to skip
      token.rune == Rune.newline -> ParsingMode.header to foldStack
      else -> parsingError(TextId.expectedPeriodOrNewline)
    }
