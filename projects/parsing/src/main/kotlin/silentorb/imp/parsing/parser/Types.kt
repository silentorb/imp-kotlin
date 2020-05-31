package silentorb.imp.parsing.parser

import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.parser.expressions.IntermediateExpression
import java.net.URI

const val localPath = ""

data class TokenizedImport(
    val path: Tokens
)

data class TokenizedParameter(
    val name: String,
    val type: String
)

data class TokenizedDefinition(
    val symbol: Token,
    val parameters: List<TokenizedParameter>,
    val expression: Tokens
)

data class TokenizedGraph(
    val imports: List<TokenizedImport>,
    val definitions: List<TokenizedDefinition>
)

data class DefinitionFirstPass(
    val tokenized: TokenizedDefinition,
    val intermediate: IntermediateExpression
)
