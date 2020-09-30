package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.base.swing.UiUtil
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

/**
 * Contains a list of [JButton]s that allows the user to select an [Editor]'s current [Tool].
 */
class ToolBar(val editor: Editor? = null) : JToolBar() {

	companion object {
		private const val SEPARATOR_WIDTH = 15
	}

	private val toolGroup = ButtonGroup()

	init {
		layout = BoxLayout(this, BoxLayout.X_AXIS)
	}

	fun addTool(tool: Tool, imgPath: String, tooltipText: String) {
		if (editor == null) {
			throw IllegalStateException("Cannot add Tool to ToolBar without Editor")
		}
		val button = createButton(imgPath, tooltipText)
		val toolListener = ToolListener(tool, button)
		button.isEnabled = editor.active
		button.addActionListener(toolListener)
		editor.addPropertyChangeListener(toolListener)
		toolGroup.add(button)
		add(button)
	}

	private fun createButton(imgPath: String, tooltipText: String): JToggleButton {
		val button = JToggleButton(UiUtil.themedIcon(imgPath))
		button.toolTipText = tooltipText
		return button
	}

	private inner class ToolListener(val tool: Tool, val button: JToggleButton) : ActionListener, PropertyChangeListener<Any> {

		override fun actionPerformed(e: ActionEvent?) {
			editor?.currentTool = tool
		}

		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == Editor.PROP_CURRENT_TOOL && e.newValue == tool) {
				if (!button.isSelected) {
					button.doClick()
					button.requestFocus()
				}
			} else if (e.name == Editor.PROP_ACTIVE) {
				button.isEnabled = editor?.active ?: false
			}
		}
	}
}