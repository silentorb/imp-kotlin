package silentorb.imp.execution

import silentorb.imp.core.Key
import silentorb.imp.core.PathKey

typealias FunctionImplementation = (Map<Key, Any>) -> Any

typealias FunctionImplementationMap = Map<PathKey, FunctionImplementation>
