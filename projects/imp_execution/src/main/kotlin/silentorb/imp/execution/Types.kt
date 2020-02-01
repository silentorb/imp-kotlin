package silentorb.imp.execution

import silentorb.imp.core.FunctionKey
import silentorb.imp.core.Key

typealias FunctionImplementation = (Map<Key, Any>) -> Any

typealias FunctionImplementationMap = Map<FunctionKey, FunctionImplementation>
