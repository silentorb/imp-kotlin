package silentorb.imp.core

data class Dependency<T>(
    val dependent: T,
    val provider: T
)

enum class DependencyError {
  circularDependency
}

tailrec fun <T> arrangeDependencies(modules: Set<T>, dependencies: Set<Dependency<T>>, accumulator: List<T>): Pair<List<T>, List<DependencyError>> =
    if (modules.none())
      Pair(accumulator, listOf())
    else {
      val nextModules = modules
          .filter { module -> dependencies.none { it.dependent == module } }

      if (nextModules.none())
        Pair(accumulator, listOf(DependencyError.circularDependency))
      else {
        val nextDependencies = dependencies
            .filter { dependency -> !nextModules.contains(dependency.provider) }
            .toSet()

        arrangeDependencies(modules - nextModules, nextDependencies, accumulator + nextModules)
      }
    }

fun <T> arrangeDependencies(modules: Set<T>, dependencies: Set<Dependency<T>>): Pair<List<T>, List<DependencyError>> =
    arrangeDependencies(modules, dependencies, listOf())
