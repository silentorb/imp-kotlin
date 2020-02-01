plugins {
  kotlin("jvm") version Versions.kotlin
}

dependencies {
  implementation(kotlin("stdlib"))
  testImplementation("junit:junit:4.13")
  testImplementation(project(":imp_standard_library"))
}

repositories {
  jcenter()
}

requires(project, "imp_core")
