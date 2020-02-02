import java.nio.file.Files

//rootProject.name = "imp"

Files.list(file("projects").toPath())
    .forEach { path ->
      include(path.fileName.toString())
      project(":${path.fileName}").projectDir = path.toFile()
    }
