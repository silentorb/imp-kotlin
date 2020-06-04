package silentorb.imp.parsing.structure

import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.lexer.Rune

fun parseHeader(token: Token): ParsingTransition =
    when {
      token.rune == Rune.identifier && token.value == "import" -> addChild(BurgType.importKeyword, ParsingMode.firstImportPathToken)
      else -> invalidToken
    }

fun parseFirstImportPathToken(token: Token): ParsingTransition =
    when {
      token.rune == Rune.identifier -> addChild(BurgType.importPathToken, ParsingMode.followingImportPathToken)
      token.rune == Rune.wildcard -> addChild(BurgType.importPathToken, ParsingMode.header)
      token.rune == Rune.newline -> resetStack(ParsingMode.header)
      else -> invalidToken
    }

fun parseStructure(token: Token, mode: ParsingMode): ParsingTransition {
  val action =
      when (mode) {
        ParsingMode.header -> ::parseHeader
        ParsingMode.firstImportPathToken -> ::parseFirstImportPathToken
      }

  return action(token)
}

fun parseStructure(tokens: Token): Realm {

}
