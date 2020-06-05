package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.syntax.*

fun parseHeader(token: Token): ParsingStep =
    when {
      isImport(token) -> ParsingStep(push(BurgType.importClause, asMarker), ParsingMode.importFirstPathToken)
      isLet(token) -> startDefinition
      isNewline(token) -> ParsingStep(fold, ParsingMode.header)
      else -> parsingError(TextId.expectedImportOrLetKeywords)
    }

fun parseImportFirstPathToken(token: Token): ParsingStep =
    when {
      isIdentifier(token) -> firstImportPathToken
      isWildcard(token) -> importPathWildcard
      isNewline(token) -> parsingError(TextId.missingImportPath)
      else -> parsingError(TextId.expectedIdentifier)
    }

fun parseImportFollowingPathToken(token: Token): ParsingStep =
    when {
      isIdentifier(token) -> followingImportPathToken
      isWildcard(token) -> importPathWildcard
      else -> parsingError(TextId.expectedIdentifierOrWildcard)
    }

fun parseImportSeparator(token: Token): ParsingStep =
    when {
      isDot(token) -> ParsingStep(skip, ParsingMode.importFollowingPathToken)
      isNewline(token) -> ParsingStep(fold, ParsingMode.header)
      else -> parsingError(TextId.expectedPeriodOrNewline)
    }
