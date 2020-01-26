package silentorb.imp.execution

import silentorb.imp.core.Key
import silentorb.imp.core.Path

typealias FunctionImplementation = (Map<Key, Any>) -> Any

typealias FunctionImplementationMap = Map<Path, FunctionImplementation>
