package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.syntax.*

fun parseHeader(token: Token): ParsingStep =
    when {
      isImport(token) -> ParsingStep(pushChild, ParsingMode.importFirstPathToken, BurgType.importKeyword)
      isLet(token) -> ParsingStep(skip, ParsingMode.definitionName)
      isNewline(token) -> ParsingStep(foldStack, ParsingMode.header)
      else -> parsingError(TextId.expectedImportOrLetKeywords)
    }

fun parseImportFirstPathToken(token: Token): ParsingStep =
    when {
      isIdentifier(token) -> ParsingStep(pushChild, ParsingMode.importSeparator, BurgType.importPathToken)
      isWildcard(token) -> ParsingStep(pushChild, ParsingMode.header, BurgType.importPathToken)
      isNewline(token) -> parsingError(TextId.missingImportPath)
      else -> parsingError(TextId.expectedIdentifier)
    }

fun parseImportFollowingPathToken(token: Token): ParsingStep =
    when {
      isIdentifier(token) -> ParsingStep(addSibling, ParsingMode.importSeparator, BurgType.importPathToken)
      isWildcard(token) -> ParsingStep(addSibling, ParsingMode.header, BurgType.importPathWildcard)
      else -> parsingError(TextId.expectedIdentifierOrWildcard)
    }

fun parseImportSeparator(token: Token): ParsingStep =
    when {
      isDot(token) -> ParsingStep(skip, ParsingMode.importFollowingPathToken)
      isNewline(token) -> ParsingStep(foldStack, ParsingMode.header)
      else -> parsingError(TextId.expectedPeriodOrNewline)
    }
