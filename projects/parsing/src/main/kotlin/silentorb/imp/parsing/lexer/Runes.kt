package silentorb.imp.parsing.lexer

enum class Rune {
  assignment,
  bad,
  colon,
  comment,
  dot,
  identifier,
  keyword,
  literalFloat,
  literalInteger,
  newline,
  operator,
  parenthesesClose,
  parenthesesOpen,
  whitespace,
  wildcard,
}
