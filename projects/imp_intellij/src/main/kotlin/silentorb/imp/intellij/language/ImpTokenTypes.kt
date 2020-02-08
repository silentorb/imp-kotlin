package silentorb.imp.intellij.language

import com.intellij.lang.ASTNode
import com.intellij.psi.PsiElement
import com.intellij.psi.tree.IElementType

object ImpTokenTypes {
  val Comment: IElementType = ImpTokenType("comment")
}

fun nodeToElement(node: ASTNode): PsiElement {
  throw AssertionError("Not yet implemented")
}
