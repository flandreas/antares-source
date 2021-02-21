package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.invocation.ErrorHandler
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
 * and stores the its location and size when being closed..
 */
abstract class AbstractApplicationFrame(
    val application: DesktopApplication,
    eventBus: EventBus = BaseModule.eventBus,
    toolbars: List<ToolBar> = emptyList()
) : JFrame() {

    abstract val editor: Editor

    companion object {
    	const val SETTING_FRAME_X = "application.frame.x"
	    const val SETTING_FRAME_Y = "application.frame.y"
	    const val SETTING_FRAME_W = "application.frame.w"
	    const val SETTING_FRAME_H = "application.frame.h"
    }

    init {
	    ErrorHandler.initialize(this, EditAuthModule.userHolder.user.isDeveloper)
        defaultCloseOperation = WindowConstants.DO_NOTHING_ON_CLOSE
        title = application.displayName

        eventBus.register(CurrentSavableEvent::class) { updateTitle() }

	    if (storedDimensionsFitInScreen()) {
		    setBounds(
	            BaseModule.settings.getInt(SETTING_FRAME_X, 0),
	            BaseModule.settings.getInt(SETTING_FRAME_Y, 0),
	            BaseModule.settings.getInt(SETTING_FRAME_W, Toolkit.getDefaultToolkit().screenSize.width),
	            BaseModule.settings.getInt(SETTING_FRAME_H, Toolkit.getDefaultToolkit().screenSize.height)
	        )
	    } else {
		    setBoundsToScreenSize()
	    }

        buildUI(toolbars)
    }

    override fun dispose() {
        super.dispose()
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

	/**
	 * Checks if the frame dimensions currently stored in the settings can be displayed on the screen.
	 * If not, the application is most probably opened on a smaller screen than when it was closed the last time.
	 */
	private fun storedDimensionsFitInScreen(): Boolean {
		val screenWidth = Toolkit.getDefaultToolkit().screenSize.width
		val screenHeight = Toolkit.getDefaultToolkit().screenSize.height
		val x = BaseModule.settings.getInt(SETTING_FRAME_X, 0)
		val y = BaseModule.settings.getInt(SETTING_FRAME_Y, 0)
		val w = BaseModule.settings.getInt(SETTING_FRAME_W, screenWidth)
		val h = BaseModule.settings.getInt(SETTING_FRAME_H, screenHeight)

		return Rectangle2D(0, 0, screenWidth, screenHeight).contains(Rectangle2D(x, y, w, h))
	}

	private fun setBoundsToScreenSize() {
		setBounds(0, 0, Toolkit.getDefaultToolkit().screenSize.width, Toolkit.getDefaultToolkit().screenSize.height)
	}
}