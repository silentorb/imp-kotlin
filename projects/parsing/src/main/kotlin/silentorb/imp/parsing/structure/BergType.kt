package silentorb.imp.parsing.structure

enum class BurgType {
  assignment,
  bad,
  colon,
  comment,
  dot,
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
