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
          .filter { module -> dependencies.none { it.dependent == module && modules.contains(it.provider) } }

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

tailrec fun <T> getCascadingDependencies(
    dependencies: Set<Dependency<T>>,
    dependents: Set<T>,
    accumulator: List<T> = listOf()
): List<T> =
    if (dependents.none())
      accumulator
    else {
      val nextDependents = dependencies
          .filter { dependency -> dependents.contains(dependency.dependent) }
          .map { dependency -> dependency.provider }
          .toSet()
          .minus(accumulator)

      val nextAccumulator = nextDependents.toList() + accumulator

      getCascadingDependencies(dependencies, nextDependents, nextAccumulator)
    }
