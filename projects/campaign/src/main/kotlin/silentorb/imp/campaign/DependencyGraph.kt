package silentorb.imp.campaign

typealias ModuleStages = List<List<ModuleId>>

tailrec fun arrangeModuleStages(modules: Set<ModuleId>, dependencies: Set<Dependency>, accumulator: ModuleStages): Pair<ModuleStages, List<CampaignError>> =
    if (modules.none())
      Pair(accumulator, listOf())
    else {
      val nextModules = modules
          .filter { module -> dependencies.none { it.dependent == module } }

      if (nextModules.none())
        Pair(accumulator.plusElement(modules.toList()), listOf(CampaignError(CampaignText.circularModuleDependencies)))
      else {
        val nextDependencies = dependencies
            .filter { dependency -> !nextModules.contains(dependency.provider) }
            .toSet()

        arrangeModuleStages(modules - nextModules, nextDependencies, accumulator.plusElement(nextModules))
      }
    }

fun arrangeModuleStages(modules: Set<ModuleId>, dependencies: Set<Dependency>): Pair<ModuleStages, List<CampaignError>> =
    arrangeModuleStages(modules, dependencies, listOf())
