package silentorb.imp.parsing.parser.expressions

typealias TokenIndex = Int

typealias TokenParents = Map<TokenIndex, List<TokenIndex>>

data class TokenGraph(
    val parents: TokenParents,
    val stages: List<List<TokenIndex>>
)
