package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

val parseHeader: TokenToParsingTransition = { token ->
  when {
    isImport(token) -> startImport
    isLet(token) -> startDefinition
    isNewline(token) -> closeImport
    isEndOfFile(token) -> ParsingStep(skip, ParsingMode.body)
    else -> parsingError(TextId.expectedImportOrLetKeywords)
  }
}

val parseImportFirstPathToken: TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> firstImportPathToken
    isWildcard(token) -> importPathWildcard
    isNewline(token) -> addError(TextId.missingImportPath) + closeImport
    else -> parsingError(TextId.expectedIdentifier)
  }
}

val parseImportFollowingPathToken: TokenToParsingTransition =
    { token ->
      when {
        isIdentifier(token) -> followingImportPathToken
        isWildcard(token) -> importPathWildcard
        else -> addError(TextId.expectedIdentifierOrWildcard) + closeImport
      }
    }

val parseImportSeparator: TokenToParsingTransition = { token ->
  when {
    isDot(token) -> ParsingStep(skip, ParsingMode.importFollowingPathToken)
    isNewline(token) -> closeImport
    else -> parsingError(TextId.expectedPeriodOrNewline)
  }
}
