package silentorb.imp.parsing.lexer

enum class Rune {
  assignment,
  comment,
  dot,
  identifier,
  literalFloat,
  literalInteger,
  newline,
  operator,
  parenthesisClose,
  parenthesisOpen,
  wildcard,
}
