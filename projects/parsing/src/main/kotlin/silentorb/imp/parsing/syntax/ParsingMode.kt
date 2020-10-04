package silentorb.imp.parsing.syntax

enum class ParsingMode {
  block,
  definitionAssignment,
  definitionBodyStart,
  definitionParameterColon,
  definitionParameterNameOrAssignment,
  definitionParameterType,
  definitionName,
  enumAssignment,
  enumFirstItem,
  enumFollowingItem,
  enumName,
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
