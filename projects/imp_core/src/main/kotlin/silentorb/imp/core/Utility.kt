package silentorb.imp.core

fun getGraphOutputNodes(graph: Graph): List<Id> =
    graph.nodes.filter { node -> graph.connections.none { it.source == node } }

fun getGraphOutputNode(graph: Graph): Id =
    getGraphOutputNodes(graph).first()
