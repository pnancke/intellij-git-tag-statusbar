package io.github.pnancke.gitTag4Intellij

import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidgetFactory

class GitTagStatusBarWidgetFactory : StatusBarWidgetFactory {

    override fun getId(): String = ID
    override fun getDisplayName(): String = "Git Tag 4 Status Bar"
    override fun isAvailable(project: Project): Boolean = true
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
    override fun createWidget(project: Project): StatusBarWidget = GitTagStatusWidget(project.basePath)
    override fun disposeWidget(widget: StatusBarWidget) {
        if (widget.ID() == ID) Disposer.dispose(widget)
    }
}