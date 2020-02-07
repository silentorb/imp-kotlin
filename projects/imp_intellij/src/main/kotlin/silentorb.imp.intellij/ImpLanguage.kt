package silentorb.imp.intellij

import com.intellij.lang.Language

class ImpLanguage() : Language("Imp") {
  companion object {
    val INSTANCE = ImpLanguage()
  }
}
