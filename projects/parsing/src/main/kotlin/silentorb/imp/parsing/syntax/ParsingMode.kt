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
  definitionExpression,
  importFirstPathToken,
  importFollowingPathToken,
  importSeparator,
  header,
}
