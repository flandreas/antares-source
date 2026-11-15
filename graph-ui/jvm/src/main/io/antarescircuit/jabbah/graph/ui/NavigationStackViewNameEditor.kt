package io.antarescircuit.jabbah.graph.ui

import io.antarescircuit.jabbah.base.StringUtils
import io.antarescircuit.jabbah.base.swing.ReleaseClickListenerSwing
import io.antarescircuit.jabbah.base.UUID
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.draw.graphics.Font
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.graph.ui.NavigationStackViewSwing.Companion.HEIGHT
import java.awt.Container
import java.awt.event.*
import javax.swing.JTextField
import javax.swing.SwingUtilities

/**
 * Support for in-place editing of root name.
 */
object NavigationStackViewNameEditor {

	private val TEXT_EDITOR = JTextField(15)

	private var view: NavigationStackViewSwing? = null
	private lateinit var uuid: UUID

	/** Issue 'Save' when user clicks on glass pane.*/
	private val clickListener = ClickListener()

	init {
		TEXT_EDITOR.addFocusListener(object : FocusAdapter() {
			override fun focusLost(e: FocusEvent?) {
				save()
			}
		})

		TEXT_EDITOR.addKeyListener(object : KeyAdapter() {
			override fun keyPressed(e: KeyEvent) {
				when (e.keyCode) {
					KeyEvent.VK_ENTER -> save()
					KeyEvent.VK_ESCAPE -> cancel()
				}
			}
		})
	}

	fun startEditor(
		view: NavigationStackViewSwing,
		uuid: UUID,
		name: String,
		relLocation: Point2D,
		font: Font
	) {
		cancel()
		val glassPane = SwingUtilities.getRootPane(view)?.glassPane as? Container ?: return

		this.view = view
		this.uuid = uuid

		glassPane.layout = null

		val location = SwingUtilities.convertPoint(
			view,
			Graphics2DJvm.toAwtPoint(relLocation),
			glassPane
		)

		TEXT_EDITOR.text = name
		TEXT_EDITOR.setBounds(
			location.x, location.y,
			TEXT_EDITOR.preferredSize.width, HEIGHT
		)
		TEXT_EDITOR.font = Graphics2DJvm.toAwtFont(font)
		TEXT_EDITOR.selectAll()


		clickListener.installOn(glassPane)
		glassPane.add(TEXT_EDITOR)
		glassPane.isVisible = true
		glassPane.repaint()

		SwingUtilities.invokeLater {
			if (this.view === view && TEXT_EDITOR.parent === glassPane) {
				TEXT_EDITOR.requestFocusInWindow()
			}
		}
	}

	private fun save() {
		val view = this.view ?: return

		val newName = TEXT_EDITOR.text
		if (StringUtils.isBlank(newName)) {
			cancel()
			return
		}

		stopEditing()
		val oldName = view.controller.navigationStack.rootEntry!!.content.drawing.name.value
		if (newName != oldName) {
			view.controller.changeName(newName)
		}
	}

	private fun cancel() {
		stopEditing()
	}

	fun cancelEditor(view: NavigationStackViewSwing) {
		if (this.view === view) {
			cancel()
		}
	}

	private fun stopEditing() {
		// Focus loss during removal must not save a cancelled or already completed edit.
		view = null
		val glassPane = TEXT_EDITOR.parent
		if (glassPane != null) {
			glassPane.remove(TEXT_EDITOR)
			glassPane.isVisible = false
			clickListener.uninstallFrom(glassPane)
		}
	}

	private class ClickListener : ReleaseClickListenerSwing(dragThreshold = Double.POSITIVE_INFINITY) {
		override fun clicked(e: MouseEvent) {
			save()
		}
	}
}
