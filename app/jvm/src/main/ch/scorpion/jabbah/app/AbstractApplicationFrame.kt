package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.ErrorHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * A standard application [JFrame] that updates its title when the current [Savable] changes
 * and stores the its location and size when being closed..
 */
abstract class AbstractApplicationFrame(
    val application: DesktopApplication,
    eventBus: EventBus = BaseModule.eventBus,
    toolbars: List<ToolBar> = emptyList()
) : JFrame() {

    abstract val editor: Editor

    init {
	    ErrorHandler.initialize(this, EditAuthModule.userHolder.user.isDeveloper)
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        title = application.displayName

        eventBus.register(CurrentSavableEvent::class) { updateTitle() }

	    setBounds(
            BaseModule.settings.getInt("application.frame.x", 100),
            BaseModule.settings.getInt("application.frame.y", 100),
            BaseModule.settings.getInt("application.frame.w", 1200),
            BaseModule.settings.getInt("application.frame.h", 1000)
        )

        buildUI(toolbars)
    }

    override fun dispose() {
        super.dispose()
        BaseModule.settings.set("application.frame.x", x)
        BaseModule.settings.set("application.frame.y", y)
        BaseModule.settings.set("application.frame.w", width)
        BaseModule.settings.set("application.frame.h", height)
    }

    /** ---- [AbstractApplicationFrame] */

    /**
     * Determines whether the current application data has been changed.
     * This implementation returns `false` always. Applications with distinctive behaviour can override this property
     * in order to implement the appropriate behaviour.
     */
    open val applicationDataChanged: Boolean get() = false

    protected open fun buildUI(toolbars: List<ToolBar>) {
        layout = BorderLayout()
        addToolbars(toolbars)
    }

    private fun addToolbars(toolbars: List<ToolBar>) {
        if (toolbars.isNotEmpty()) {
            val toolbarPanel = JPanel()
            toolbarPanel.layout = BoxLayout(toolbarPanel, BoxLayout.LINE_AXIS)
            toolbars.forEach { toolbarPanel.add(it) }
            add(toolbarPanel, BorderLayout.NORTH)
        }
    }

    private fun updateTitle() {
        title = if (application.controller.data != null) {
            "${application.displayName} - ${application.controller.data!!.savable.description}"
        } else {
            application.displayName
        }
    }
}