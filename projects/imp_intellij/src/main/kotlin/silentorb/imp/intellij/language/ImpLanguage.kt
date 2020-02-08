package silentorb.imp.intellij.language

import com.intellij.lang.Language

class ImpLanguage() : Language("Imp") {
  companion object {
    val INSTANCE = ImpLanguage()
  }
}
