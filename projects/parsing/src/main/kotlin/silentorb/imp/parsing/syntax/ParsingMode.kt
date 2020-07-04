package silentorb.imp.parsing.syntax

enum class ParsingMode {
  block,
  definitionAssignment,
  definitionBodyStart,
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
