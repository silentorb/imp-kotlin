
dependencies {
  api(project(":core"))

  implementation("com.fasterxml.jackson.core:jackson-databind:2.10.4")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.10.4")
  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.10.4")
  implementation(group = "com.fasterxml.jackson.module", name = "jackson-module-afterburner", version = "2.10.4")
  implementation(group = "org.jetbrains.kotlin", name = "kotlin-reflect", version = "1.3.72")
}
