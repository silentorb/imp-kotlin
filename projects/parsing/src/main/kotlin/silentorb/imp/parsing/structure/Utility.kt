package silentorb.imp.parsing.structure

import silentorb.imp.core.TokenFile
import silentorb.imp.parsing.general.Token

fun newBurg(file: TokenFile, type: BurgType, token: Token): Burg =
    Burg(
        type = type,
        range = token.range,
        file = file
    )

fun newBurgEntry(file: TokenFile, type: BurgType, token: Token): Pair<BurgId, Burg> {
  val burg = newBurg(file, type, token)
  return (burg.hashCode() to burg)
}
