package silentorb.imp.parsing.parser

import silentorb.imp.core.*
import silentorb.imp.parsing.general.Response
import silentorb.imp.parsing.general.Token
import silentorb.imp.parsing.general.Tokens
import silentorb.imp.parsing.general.flatten

data class TokenizedImport(
    val path: Tokens
)

data class TokenizedDefinition(
    val symbol: Token,
    val expression: Tokens
)

data class TokenizedGraph(
    val imports: List<TokenizedImport>,
    val definitions: List<TokenizedDefinition>
)

fun parseDungeon(parentContext: Context): (TokenizedGraph) -> Response<Dungeon> =
    { (imports, definitions) ->
      val nextId = newIdSource(1L)
      val passThroughNodes = definitions.mapIndexed { index, definition ->
        Pair(nextId(), definition)
      }
          .associate { it }

      val definitionSymbols = passThroughNodes.map { (id, definition) ->
        Pair(definition.symbol.value, id)
      }
          .associate { it }

      flatten(imports.map(parseImport(parentContext.first())))
          .then { rawImportedFunctions ->
            val importedFunctions = rawImportedFunctions
                .flatten()
                .associate { it }

            val context = parentContext.plus(
                Namespace(
                    nodes = definitionSymbols,
                    functions = importedFunctions
                )
            )

            flatten(passThroughNodes.map { (id, definition) ->
              parseExpression(nextId, context, definition.expression)
                  .map { dungeon ->
                    val output = getGraphOutputNode(dungeon.graph)
                    dungeon.copy(
                        graph = dungeon.graph.copy(
                            connections = dungeon.graph.connections.plus(
                                Connection(
                                    source = output,
                                    destination = id,
                                    parameter = defaultParameter
                                )
                            )
                        )
                    )
                  }
            })
                .then { expressionDungeons ->
                  val nodes: Set<Id> = passThroughNodes.keys

                  val nodeMap = passThroughNodes
                      .mapValues { (_, definition) -> definition.symbol.range }

                  val initialGraph = Graph(
                      nodes = nodes,
                      connections = setOf(),
                      functions = mapOf(),
                      values = mapOf()
                  )

                  val initialDungeon = Dungeon(
                      graph = initialGraph,
                      nodeMap = nodeMap
                  )

                  val dungeon = expressionDungeons.fold(initialDungeon) { a, expressionDungeon ->
                    mergeDistinctDungeons(a, expressionDungeon)
                  }

                  checkForGraphErrors(dungeon.nodeMap)(dungeon.graph)
                      .map { dungeon }
                }
          }
    }
