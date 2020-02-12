dependencies {


  // Even though junit is a test library, this is also a test library
  // so junit should be included as implementation, not testImplementation
  implementation("junit:junit:4.13")
}

requires(project, "parsing")
