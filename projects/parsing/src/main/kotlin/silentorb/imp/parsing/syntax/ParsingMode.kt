package silentorb.imp.parsing.syntax

enum class ParsingMode {
  body,
  definitionAssignment,
  definitionParameterColon,
  definitionParameterNameOrAssignment,
  definitionParameterType,
  definitionName,
  expressionArgumentStart,
  expressionArgumentFollowing,
  expressionNamedArgumentValue,
  expressionStart,
  importFirstPathToken,
  importFollowingPathToken,
  importSeparator,
  header,
  pipingRootStart,
}
