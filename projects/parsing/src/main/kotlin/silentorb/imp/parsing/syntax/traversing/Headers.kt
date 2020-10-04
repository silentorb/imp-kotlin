package silentorb.imp.parsing.syntax.traversing

import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.syntax.*

val parseHeader: TokenToParsingTransition = { token ->
  when {
    isImport(token) -> startImport
    isLet(token) -> startDefinition
    isEnum(token) -> startEnum
    isNewline(token) -> closeImport
    isEndOfFile(token) -> goto(ParsingMode.block)
    else -> addError(TextId.expectedImportOrLetKeywords)
  }
}

val parseImportFirstPathToken: TokenToParsingTransition = { token ->
  when {
    isIdentifier(token) -> firstImportPathToken
    isWildcard(token) -> importPathWildcard
    isNewline(token) -> addError(TextId.missingImportPath) + closeImport
    else -> addError(TextId.expectedIdentifier)
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
    isDot(token) -> goto(ParsingMode.importFollowingPathToken)
    isNewline(token) -> closeImport
    else -> addError(TextId.expectedPeriodOrNewline)
  }
}
