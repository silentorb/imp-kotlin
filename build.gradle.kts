plugins {
  kotlin("jvm") version Versions.kotlin
}

allprojects {
  group = "silentorb.imp"
  version = "1.0"

  tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
  }

  repositories {
    jcenter()
    mavenCentral()
  }
}

subprojects {
  apply(plugin = "org.jetbrains.kotlin.jvm")

  dependencies {
    val implementation by configurations
    implementation(kotlin("stdlib-jdk8"))
  }
}
