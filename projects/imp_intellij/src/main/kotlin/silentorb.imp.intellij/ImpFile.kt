package silentorb.imp.intellij

import com.intellij.extapi.psi.PsiFileBase
import com.intellij.openapi.fileTypes.FileType
import com.intellij.psi.FileViewProvider
import silentorb.imp.intellij.language.ImpLanguage

class ImpFile(viewProvider: FileViewProvider) : PsiFileBase(viewProvider, ImpLanguage.INSTANCE) {
  override fun getFileType(): FileType {
    return ImpFileType.INSTANCE
  }

  override fun toString(): String {
    return "Imp File"
  }
}
