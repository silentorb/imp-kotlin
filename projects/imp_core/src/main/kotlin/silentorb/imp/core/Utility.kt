package silentorb.imp.core

fun getGraphOutputNode(graph: Graph): Id =
    graph.nodes.first { node -> graph.connections.none { it.source == node } }
