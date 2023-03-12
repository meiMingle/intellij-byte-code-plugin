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
import dev.turingcomplete.intellijbytecodeplugin.common.ByteCodeToolService

internal class AnalyzeByteCodeAction : DumbAwareAction(TITLE, null, ByteCodePluginIcons.ACTION_ICON) {
  // -- Companion Object -------------------------------------------------------------------------------------------- //

  companion object {
    const val TITLE = "Analyze Byte Code"
  }

  // -- Properties -------------------------------------------------------------------------------------------------- //
  // -- Initialization ---------------------------------------------------------------------------------------------- //
  // -- Exposed Methods --------------------------------------------------------------------------------------------- //

  override fun update(e: AnActionEvent) {
    e.presentation.isEnabled = CommonDataKeys.PROJECT.getData(e.dataContext)?.let { project ->
      val files = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.dataContext)
      if (files?.any { OpenClassFilesTask.isOpenableFile(it, project) } == true) {
        true
      }
      else {
        findPsiElement(project, e.dataContext).let { result ->
          val psiElement = result.first
          OpenClassFilesTask.isOpenableFile(psiElement?.containingFile)
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
      val originalFile = CommonDataKeys.VIRTUAL_FILE.getData(e.dataContext)
      project.getService(ByteCodeToolService::class.java).openPsiElements(listOf(psiElement), editorPsiFile, originalFile)
      return
    }

    val files = CommonDataKeys.VIRTUAL_FILE_ARRAY.getData(e.dataContext)
    if (files != null) {
      project.getService(ByteCodeToolService::class.java).openFiles(files.toList())
    }
  }

  override fun getActionUpdateThread() = ActionUpdateThread.EDT

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