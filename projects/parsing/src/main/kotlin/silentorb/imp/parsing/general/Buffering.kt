package silentorb.imp.parsing.general

import silentorb.imp.core.CodeInt
import java.nio.file.Path

// This file should include all code relate to whether to use Java Strings or raw memory blocks to store code
// Initially String is being used but this file could easily be changed to use raw memory for increased maximum code size

// Could be later changed to a raw memory buffer
typealias CodeBuffer = CharSequence

fun getCharFromBuffer(buffer: CodeBuffer, index: CodeInt): Char =
    buffer[index]

fun getCodeBufferSize(buffer: CodeBuffer): CodeInt =
    buffer.length
typealias GetCode = (Path) -> String?
