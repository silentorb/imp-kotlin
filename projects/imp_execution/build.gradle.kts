plugins {
  kotlin("jvm")
}

dependencies {
  implementation(kotlin("stdlib"))
  testImplementation("junit:junit:4.13")
  testImplementation(project(":imp_libraries_standard"))
  testImplementation(project(":imp_libraries_standard_implementation"))
  testImplementation(project(":imp_testing"))
}

repositories {
  jcenter()
}

requires(project, "imp_core")
