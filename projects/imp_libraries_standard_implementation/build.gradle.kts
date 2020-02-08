plugins {
  kotlin("jvm")
}

dependencies {
  implementation(kotlin("stdlib"))
}

repositories {
  jcenter()
}

requires(project, "imp_execution", "imp_libraries_standard")
