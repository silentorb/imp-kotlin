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
  expressionRootStart,
  expressionRootArguments,
  expressionStart,
  importFirstPathToken,
  importFollowingPathToken,
  importSeparator,
  header,
  subExpressionArguments,
  subExpressionStart,
}
