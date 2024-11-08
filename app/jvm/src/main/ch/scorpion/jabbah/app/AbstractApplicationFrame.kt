package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.invocation.ErrorHandler
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.auth.EditAuthModule
import java.awt.BorderLayout
import java.awt.Toolkit
import javax.swing.BoxLayout
import javax.swing.JFrame
import javax.swing.JPanel
import javax.swing.WindowConstants

/**
 * A standard application [JFrame] that updates its title when the current [Savable] changes
 * and stores its location and size when being closed.
 */
abstract class AbstractApplicationFrame(
    val application: DesktopApplication,
    eventBus: EventBus = BaseModule.eventBus,
    toolbars: List<ToolBar> = emptyList()
) : JFrame() {

    abstract val editor: Editor

    companion object {
        private val LOG by logger(AbstractApplicationFrame::class)

    	const val SETTING_FRAME_X = "application.frame.x"
	    const val SETTING_FRAME_Y = "application.frame.y"
	    const val SETTING_FRAME_W = "application.frame.w"
	    const val SETTING_FRAME_H = "application.frame.h"
    }

    init {
	    ErrorHandler.initialize(
		    this,
		    application.aboutInfo.version.toString(),
		    EditAuthModule.userHolder.user.isDeveloper)

        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        title = application.displayName

        eventBus.register(CurrentSavableEvent::class) { updateTitle() }

        val xx = BaseModule.settings.getInt(SETTING_FRAME_X, 0)
        val yy = BaseModule.settings.getInt(SETTING_FRAME_Y, 0)
        val ww = BaseModule.settings.getInt(SETTING_FRAME_W, Toolkit.getDefaultToolkit().screenSize.width)
        val hh = BaseModule.settings.getInt(SETTING_FRAME_H, Toolkit.getDefaultToolkit().screenSize.height)
        LOG.debug("Loading frame x=$xx, y=$yy, w=$ww, hh=$hh")
        setBounds(xx, yy, ww, hh)

        buildUI(toolbars)
    }

    override fun dispose() {
        super.dispose()
        LOG.debug("Storing frame x=$x, y=$y, width=$width, height=$height")
        BaseModule.settings.set(SETTING_FRAME_X, x)
        BaseModule.settings.set(SETTING_FRAME_Y, y)
        BaseModule.settings.set(SETTING_FRAME_W, width)
        BaseModule.settings.set(SETTING_FRAME_H, height)
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

	private fun setBoundsToScreenSize() {
		setBounds(0, 0, Toolkit.getDefaultToolkit().screenSize.width, Toolkit.getDefaultToolkit().screenSize.height)
	}
}