dependencies {
  implementation("org.junit.jupiter:junit-jupiter:5.6.1")
  implementation(project(":parsing"))
  testImplementation(project(":execution"))
  testImplementation(project(":libraries_standard"))
  testImplementation(project(":campaign"))
}
