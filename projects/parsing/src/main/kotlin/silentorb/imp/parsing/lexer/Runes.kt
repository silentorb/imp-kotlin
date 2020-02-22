package silentorb.imp.parsing.lexer

enum class Rune {
  assignment,
  bad,
  comment,
  dot,
  identifier,
  literalFloat,
  literalInteger,
  newline,
  operator,
  parenthesisClose,
  parenthesisOpen,
  whitespace,
  wildcard,
}
