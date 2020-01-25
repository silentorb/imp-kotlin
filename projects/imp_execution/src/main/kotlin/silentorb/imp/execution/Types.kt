package silentorb.imp.execution

import silentorb.imp.core.AbsolutePath
import silentorb.imp.core.Key

typealias FunctionImplementation = (Map<Key, Any>) -> Any

typealias FunctionImplementationMap = Map<AbsolutePath, FunctionImplementation>
