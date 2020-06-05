package silentorb.imp.parsing.syntax

import silentorb.imp.core.Dependency
import silentorb.imp.core.DependencyError
import silentorb.imp.core.TokenFile
import silentorb.imp.core.arrangeDependencies
import silentorb.imp.parsing.general.ParsingResponse
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.lexer.Rune
import silentorb.imp.parsing.parser.*
import java.nio.file.Paths

fun arrangeRealm(realm: Realm): Pair<List<BurgId>, List<DependencyError>> {
  val dependencies = realm.burgs.mapValues { it.value.children }
      .flatMap { (parent, children) ->
        children.map { child ->
          Dependency(child, parent)
        }
      }
      .toSet()

  return arrangeDependencies(realm.burgs.keys, dependencies)
}

fun withoutComments(tokens: Tokens): Tokens =
    tokens.filter { it.rune != Rune.comment }

fun toTokenGraph(file: TokenFile, tokens: Tokens): ParsingResponse<TokenDungeon> {
  val (realm, errors) = parseSyntax(file, tokens)
  val burgs = realm.burgs
  val lookUpBurg = { id: BurgId -> burgs[id]!! }
  val rootChildren = getExpandedChildren(realm, realm.root)

  val imports = rootChildren
      .filter { it.type == BurgType.importClause }
      .map { importBurg ->
        TokenizedImport(
            path = importBurg.children.map(lookUpBurg)
        )
      }

  val definitions = rootChildren
      .filter { it.type == BurgType.definition }
      .mapNotNull { definition ->
        val definitionChildren = getExpandedChildren(realm, definition.hashCode())

        val name = definitionChildren.firstOrNull { it.type == BurgType.definitionName }
        if (name != null) {
          val parameters = definitionChildren
              .filter { it.type == BurgType.parameter }
              .mapNotNull { parameter ->
                val parameterChildren = getExpandedChildren(realm, parameter.hashCode())
                if (parameterChildren.size == 2)
                  TokenParameter(
                      parameterChildren.first(),
                      parameterChildren.last()
                  )
                else
                  null
              }

          val expressionBurg = definitionChildren.firstOrNull { it.type == BurgType.expression }
          if (expressionBurg != null) {
            val suburbs = subRealm(realm.roads, expressionBurg.hashCode())
            val expression = realm.copy(
                root = expressionBurg.hashCode(),
                burgs = realm.burgs.filterKeys { suburbs.contains(it) }
            )
            TokenizedDefinition(
                file = Paths.get(file),
                symbol = name,
                parameters = parameters,
                expression = expression
            )
          } else
            null
        } else
          null
      }
  val graph = TokenDungeon(
      imports = imports,
      definitions = definitions
  )

  return ParsingResponse(graph, errors)
}
