package dev.turingcomplete.intellijbytecodeplugin.openclassfiles._internal

import com.intellij.openapi.vfs.VirtualFile
import dev.turingcomplete.intellijbytecodeplugin.common.SourceFile

data class ProcessableClassFile(val classFile: VirtualFile, val sourceFile: SourceFile? = null) {
  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //

  init {
    assert(classFile.extension == "class")
  }

  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  fun refreshValidity(): Boolean {
    classFile.refresh(false, false)
    return classFile.isValid
  }

  // -- Private Methods --------------------------------------------------------------------------------------------- //
  // -- Inner Type -------------------------------------------------------------------------------------------------- //
  // -- Companion Object -------------------------------------------------------------------------------------------- //
}