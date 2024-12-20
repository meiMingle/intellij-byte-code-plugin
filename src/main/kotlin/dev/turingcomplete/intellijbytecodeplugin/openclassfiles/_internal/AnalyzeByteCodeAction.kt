package dev.turingcomplete.intellijbytecodeplugin.openclassfiles._internal

import com.intellij.injected.editor.EditorWindow
import com.intellij.lang.injection.InjectedLanguageManager
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiUtilBase
import dev.turingcomplete.intellijbytecodeplugin._ui.ByteCodePluginIcons
import dev.turingcomplete.intellijbytecodeplugin.common.ByteCodeAnalyserOpenClassFileService

internal class AnalyzeByteCodeAction : DumbAwareAction(TITLE, null, ByteCodePluginIcons.ACTION_ICON) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    const val TITLE = "Analyze Byte Code"
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabledAndVisible = CommonDataKeys.PROJECT.getData(e.dataContext)?.let { project ->
      val files = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.dataContext)
      if (files?.any { ClassFilesFinderService.fileCanBeAnalysed(it, project) } == true) {
        true
      }
      else {
        findPsiElement(project, e.dataContext).let { result ->
          val containingFile = result.first?.containingFile
          if (containingFile != null) ClassFilesFinderService.fileCanBeAnalysed(containingFile) else false
        }
      }
    } ?: false
  }

  override fun actionPerformed(e: AnActionEvent) {
    val project = CommonDataKeys.PROJECT.getData(e.dataContext) ?: return

    val result = findPsiElement(project, e.dataContext)
    val psiElement = result.first
    val editorPsiFile = result.second
    if (psiElement != null) {
      project.getService(ByteCodeAnalyserOpenClassFileService::class.java).openPsiElements(mapOf(psiElement to editorPsiFile))
      return
    }

    val files = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.dataContext)
    if (files != null) {
      project.getService(ByteCodeAnalyserOpenClassFileService::class.java).openVirtualFiles(files.toList())
    }
  }

  override fun getActionUpdateThread() = ActionUpdateThread.BGT

  // -- Private Methods --------------------------------------------------------------------------------------------- //

  private fun findPsiElement(project: Project, dataContext: DataContext): Pair<PsiElement?, PsiFile?> {
    val editor = dataContext.getData(CommonDataKeys.EDITOR)
                 ?: return Pair(dataContext.getData(CommonDataKeys.PSI_ELEMENT), dataContext.getData(CommonDataKeys.PSI_FILE))

    val editorPsiFile = PsiUtilBase.getPsiFileInEditor(editor, project)
    val psiElement = findPsiElementInInjectedEditor(editor, editorPsiFile, project)
                     ?: editorPsiFile?.findElementAt(editor.caretModel.offset)
    return Pair(psiElement, editorPsiFile)
  }

  private fun findPsiElementInInjectedEditor(editor: Editor, editorPsiFile: PsiFile?, project: Project): PsiElement? {
    if (editorPsiFile == null || editor is EditorWindow) {
      return null
    }

    val offset = editor.caretModel.offset
    return InjectedLanguageManager.getInstance(project).findInjectedElementAt(editorPsiFile, offset)
            ?.containingFile
            ?.findElementAt(editor.caretModel.offset)
  }

  // -- Inner Type -------------------------------------------------------------------------------------------------- //
}