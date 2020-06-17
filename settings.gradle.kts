import java.nio.file.Files

Files.list(file("projects").toPath())
    .forEach { path ->
      include(path.fileName.toString())
      project(":${path.fileName}").projectDir = path.toFile()
    }

includeBuild("../mythic/modules/debugging")
