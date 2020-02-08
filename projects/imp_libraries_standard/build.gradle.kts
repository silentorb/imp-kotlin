plugins {
  kotlin("jvm")
}

dependencies {
  implementation(kotlin("stdlib"))
}

repositories {
  jcenter()
}

requires(project, "imp_core")
