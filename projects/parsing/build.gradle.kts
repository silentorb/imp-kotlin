plugins {
  kotlin("jvm")
}

dependencies {
  implementation(kotlin("stdlib"))
  testImplementation("junit:junit:4.13")
  testImplementation(project(":libraries_standard"))
  testImplementation(project(":testing"))
}

requires(project, "core")
