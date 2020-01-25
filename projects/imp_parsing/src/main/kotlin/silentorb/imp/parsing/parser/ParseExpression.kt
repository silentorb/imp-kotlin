package silentorb.imp.parsing.parser

import silentorb.imp.core.Context
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens

fun parseExpression(context: Context, tokens: Tokens): Token {
  return tokens.first()
}
