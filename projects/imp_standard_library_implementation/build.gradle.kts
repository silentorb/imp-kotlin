plugins {
  kotlin("jvm") version Versions.kotlin
}

dependencies {
  implementation(kotlin("stdlib"))
}

repositories {
  jcenter()
}

requires(project, "imp_execution", "imp_standard_library")
