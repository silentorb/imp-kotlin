package silentorb.imp.intellij.language

import com.intellij.lexer.Lexer
import com.intellij.lexer.LexerPosition
import com.intellij.psi.tree.IElementType

class ImpLexerPosition : LexerPosition {
  override fun getState(): Int {
    return 0
  }

  override fun getOffset(): Int {
    return 0
  }
}

class ImpLexer : Lexer() {
  var buffer: CharSequence? = null
  var stringBuffer: String? = null

  override fun getState(): Int {
    return 0
  }

  override fun getTokenStart(): Int {
    return 0
  }

  override fun getBufferEnd(): Int {
    return 0
  }

  override fun getCurrentPosition(): LexerPosition {
    return ImpLexerPosition()
  }

  override fun getBufferSequence(): CharSequence {
    return buffer!!
  }

  override fun start(buffer: CharSequence, startOffset: Int, endOffset: Int, initialState: Int) {
    this.buffer = buffer
  }

  override fun getTokenType(): IElementType? {
    return null
  }

  override fun advance() {
    val k = 0
  }

  override fun getTokenEnd(): Int {
    return 0
  }

  override fun restore(position: LexerPosition) {

  }
}
