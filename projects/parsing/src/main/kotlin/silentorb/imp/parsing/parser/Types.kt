package silentorb.imp.parsing.parser

import silentorb.imp.core.PathKey
import silentorb.imp.parsing.resolution.IntermediateExpression
import silentorb.imp.parsing.syntax.Burg
import silentorb.imp.parsing.syntax.BurgId
import silentorb.imp.parsing.syntax.Realm
import java.nio.file.Path

const val localPath = ""

data class TokenizedImport(
    val path: List<Burg>
)

data class TokenParameter(
    val name: Burg,
    val type: Burg
)

data class TokenExpression(
    val realm: Realm,
    val burgOrder: List<BurgId>
)

data class TokenizedDefinition(
    val file: Path,
    val symbol: Burg,
    val parameters: List<TokenParameter>,
    val expression: Realm
)

data class TokenDungeon(
    val imports: List<TokenizedImport>,
    val definitions: List<TokenizedDefinition>
)

data class DefinitionFirstPass(
    val file: Path,
    val key: PathKey,
    val tokenized: TokenizedDefinition,
    val intermediate: IntermediateExpression
)
