package io.github.pnancke.gitTag4Intellij

import com.intellij.ide.lightEdit.LightEditCompatible
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.util.Consumer
import com.intellij.util.concurrency.AppExecutorUtil
import org.jetbrains.annotations.SystemIndependent
import java.awt.Component
import java.awt.event.MouseEvent
import java.io.File
import java.util.concurrent.TimeUnit

const val ID = "Git Tag 4 Status Bar"

class GitTagStatusWidget(basePath: @SystemIndependent String?) : StatusBarWidget, StatusBarWidget.TextPresentation, LightEditCompatible {
    private var statusBar: StatusBar? = null
    private val log: Logger = Logger.getInstance(this.javaClass)
    private val projectDir = if (basePath != null) File(basePath) else null

    private fun retrieveGitTagData(): String {
        log.debug("Fetch Git Tags")
        return if (projectDir != null) {
            projectDir.execute("git", "fetch", "--tags")
            try {
                projectDir.execute("git", "describe", "--tags", "--abbrev=0")
            } catch (exception: Exception) {
                "No tags found"
            }
        } else {
            "Error"
        }
    }

    init {
        val future = AppExecutorUtil.getAppScheduledExecutorService().scheduleWithFixedDelay({
            runInEdt(this) { statusBar?.updateWidget(ID) }
        }, 0, 60, TimeUnit.SECONDS)
        Disposer.register(this, Disposable { future.cancel(false) })
    }

    override fun ID(): String = ID
    override fun getPresentation(): StatusBarWidget.WidgetPresentation? = this
    override fun getTooltipText(): String? = if (projectDir == null) "No basePath set!" else ""
    override fun getText(): String = retrieveGitTagData()
    override fun getClickConsumer(): Consumer<MouseEvent>? = null
    override fun getAlignment(): Float = Component.CENTER_ALIGNMENT

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar
    }

    override fun dispose() {
        statusBar = null
    }

    private fun File.execute(vararg arguments: String): String {
        val process = ProcessBuilder(*arguments)
            .directory(this)
            .start()
            .also { it.waitFor(10, TimeUnit.SECONDS) }

        if (process.exitValue() != 0) {
            throw Exception(process.errorStream.bufferedReader().readText())
        }
        return process.inputStream.bufferedReader().readText()
    }

    private fun runInEdt(disposable: Disposable, action: () -> Unit) {
        ApplicationManager.getApplication().invokeLater(action, { Disposer.isDisposed(disposable) })
    }
}

