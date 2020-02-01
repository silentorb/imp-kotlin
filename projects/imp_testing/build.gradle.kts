plugins {
  kotlin("jvm") version Versions.kotlin
}

dependencies {
  implementation(kotlin("stdlib"))
  implementation("junit:junit:4.13")
}

repositories {
  jcenter()
}

requires(project, "imp_parsing")
