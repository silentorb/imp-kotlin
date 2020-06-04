package silentorb.imp.parsing.syntax

enum class BurgType {
  assignment,
  bad,
  colon,
  comment,
  definitionName,
  dot,
  fileRoot,
  importKeyword,
  importPathToken,
  importPathWildcard,
  keyword,
  literalFloat,
  literalInteger,
  newline,
  operator,
  parenthesesClose,
  parenthesesOpen,
  reference,
}
