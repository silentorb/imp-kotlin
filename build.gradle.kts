plugins {
  kotlin("jvm") version Versions.kotlin
}

allprojects {
  group = "silentorb.imp"
  version = "1.0"

  repositories {
    jcenter()
    mavenCentral()
  }
}
