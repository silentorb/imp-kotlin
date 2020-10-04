package silentorb.imp.parsing.syntax

import silentorb.imp.core.*
import silentorb.imp.parsing.general.TextId
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.general.newParsingError
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.*
import java.nio.file.Paths

fun arrangeRealm(realm: Realm): Pair<List<Burg>, List<DependencyError>> {
  val dependencies = realm.burgs
      .flatMap { parent ->
        parent.children.map { child ->
          Dependency(child, parent)
        }
      }
      .toSet()

  return arrangeDependencies(realm.burgs, dependencies)
}

fun withoutComments(tokens: Tokens): Tokens =
    tokens.filter { it.rune != Rune.comment }

fun definitionToTokenGraph(realm: Realm, file: TokenFile): (Burg) -> TokenizedDefinition? = { definition ->

  val name = definition.children.firstOrNull { it.type == BurgType.burgName }
  if (name != null) {
    val parameters = definition.children
        .filter { it.type == BurgType.parameter }
        .mapNotNull { parameter ->
          if (parameter.children.size == 2)
            TokenParameter(
                parameter.children.first(),
                parameter.children.last()
            )
          else
            null
        }
    val blockBurg = definition.children.firstOrNull { it.type == BurgType.block }
    if (blockBurg != null) {
      val definitions = blockBurg.children
          .mapNotNull(definitionToTokenGraph(realm, file))
      assert(definitions.any())
      TokenizedDefinition(
          file = Paths.get(file),
          symbol = name,
          parameters = parameters,
          definitions = definitions
      )
    } else {
      val expressionBurg = definition.children.firstOrNull {
        it.type != BurgType.burgName && it.type != BurgType.parameter
      }

      if (expressionBurg != null) {
        val suburbs = subRealm(realm.burgs, expressionBurg)
        val expression = realm.copy(
            root = expressionBurg,
            burgs = realm.burgs.filter { suburbs.contains(it) }.toSet()
        )
        TokenizedDefinition(
            file = Paths.get(file),
            symbol = name,
            parameters = parameters,
            expression = expression
        )
      } else
        null
    }
  } else
    null
}

fun toTokenGraph(file: TokenFile, tokens: Tokens): Response<TokenDungeon> {
  val (realm, syntaxErrors) = parseSyntax(file, tokens)
  return if (realm == null)
    Response(TokenDungeon(listOf(), listOf()), syntaxErrors)
  else {
    val root = realm.root

    val imports = root.children
        .filter { it.type == BurgType.importClause }
        .map { importBurg ->
          TokenizedImport(
              path = importBurg.children
          )
        }

    val definitions = root.children
        .filter { it.type == BurgType.definition }
        .mapNotNull(definitionToTokenGraph(realm, file))

    val duplicateSymbolErrors = definitions
        .groupBy { it.symbol.value }
        .filter { it.value.size > 1 }
        .map { newParsingError(TextId.duplicateSymbol, it.value.last().symbol) }

    val graph = TokenDungeon(
        imports = imports,
        definitions = definitions
    )

    Response(graph, syntaxErrors + duplicateSymbolErrors)
  }
}
