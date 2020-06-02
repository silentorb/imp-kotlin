package silentorb.imp.parsing.parser

import silentorb.imp.core.PathKey
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.parser.expressions.IntermediateExpression
import java.net.URI
import java.nio.file.Path

const val localPath = ""

data class TokenizedImport(
    val importToken: Token,
    val path: Tokens
)

data class TokenizedParameter(
    val name: String,
    val type: String
)

data class TokenizedDefinition(
    val file: Path,
    val symbol: Token,
    val parameters: List<TokenizedParameter>,
    val expression: Tokens
)

data class TokenGraph(
    val imports: List<TokenizedImport>,
    val definitions: List<TokenizedDefinition>
)

data class DefinitionFirstPass(
    val file: Path,
    val key: PathKey,
    val tokenized: TokenizedDefinition,
    val intermediate: IntermediateExpression
)
