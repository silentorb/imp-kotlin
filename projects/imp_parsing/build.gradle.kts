plugins {
  kotlin("jvm") version Versions.kotlin
}

dependencies {
  implementation(kotlin("stdlib"))
  testImplementation("junit:junit:4.13")
  testImplementation(project(":imp_standard_library"))
  testImplementation(project(":imp_testing"))
}

repositories {
  jcenter()
}

requires(project, "imp_core")
