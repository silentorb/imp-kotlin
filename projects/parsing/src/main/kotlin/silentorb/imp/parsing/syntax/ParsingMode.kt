package silentorb.imp.parsing.syntax

enum class ParsingMode {
  body,
  definitionAssignment,
  definitionParameterColon,
  definitionParameterSeparatorOrAssignment,
  definitionParameterName,
  definitionParameterNameOrAssignment,
  definitionParameterType,
  definitionName,
  descend,
  expression,
  importFirstPathToken,
  importFollowingPathToken,
  importSeparator,
  header,
  subExpression,
}
