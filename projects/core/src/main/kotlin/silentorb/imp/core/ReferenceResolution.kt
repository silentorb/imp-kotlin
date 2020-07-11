package silentorb.imp.core

tailrec fun resolveReferenceValue(context: Context, key: PathKey): Any? {
  val value = getValue(context, key)
  return if (value != null)
    value
  else {
    val connections = getInputConnections(context, key)
    val isSingleConnection = connections.size == 1 && connections.keys.first().parameter == defaultParameter
    if (!isSingleConnection) {
      null
    } else {
      val reference = connections.values.first()
      resolveReferenceValue(context, reference)
    }
  }
}
