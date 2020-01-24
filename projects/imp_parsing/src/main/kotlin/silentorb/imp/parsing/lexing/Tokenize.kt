package silentorb.imp.parsing.lexing

import silentorb.imp.parsing.success
import silentorb.imp.parsing.Response
import silentorb.imp.parsing.handle

typealias LexicalBuffer = String

tailrec fun nextToken(code: String, position: Position, buffer: LexicalBuffer): Response<Token?> {
  return success(null)
}

tailrec fun tokenize(code: String, position: Position, tokens: Tokens): Response<Tokens> {
  return handle(nextToken(code, position, "")) { token ->
    val result = if (token != null) tokens.plus(token) else tokens
    success(result)
  }
}
