
dependencies {
  api(project(":parsing"))
  api(project(":execution"))

  implementation("com.fasterxml.jackson.core:jackson-databind:2.11.0")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.11.0")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.11.0")
  implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-afterburner", version = "2.11.0")
  implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.3.72")
}
