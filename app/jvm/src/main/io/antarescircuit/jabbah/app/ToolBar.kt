package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.base.Action
import io.antarescircuit.jabbah.base.ActionWrapperSwing
import io.antarescircuit.jabbah.base.event.PropertyChangeEvent
import io.antarescircuit.jabbah.base.event.PropertyChangeListener
import io.antarescircuit.jabbah.base.swing.UiUtil
import io.antarescircuit.jabbah.draw.View
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.Tool
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

/**
 * Contains a list of [JButton]s that allows the user to select an [Editor]'s current [Tool].
 */
open class ToolBar(val editor: Editor? = null) : JToolBar() {

	companion object {
		private const val GAP_WIDTH = 5
	}

	private val toolGroup = ButtonGroup()

	init {
		layout = BoxLayout(this, BoxLayout.X_AXIS)
	}

	fun addTool(tool: Tool, imgPath: String, tooltipText: String) {
		if (editor == null) {
			throw IllegalStateException("Cannot add Tool to ToolBar without Editor")
		}
		val button = createToggleButton(imgPath, tooltipText)
		val toolListener = ToolListener(tool, button)
		button.isEnabled = editor.active
		button.addActionListener(toolListener)
		editor.addPropertyChangeListener(toolListener)
		editor.view.addPropertyChangeListener(toolListener)
		toolGroup.add(button)
		add(button)
	}

	fun addAction(action: Action) {
		add(JButton(ActionWrapperSwing(action)))
	}

	fun addGap() {
		add(Box.createHorizontalStrut(GAP_WIDTH))
	}

	private fun createToggleButton(imgPath: String, tooltipText: String): JToggleButton {
		val button = JToggleButton(UiUtil.themedIcon(imgPath))
		button.toolTipText = tooltipText
		return button
	}

	private inner class ToolListener(val tool: Tool, val button: JToggleButton) : ActionListener, PropertyChangeListener<Any> {

		override fun actionPerformed(e: ActionEvent?) {
			editor?.currentTool = tool
		}

		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			when(e.source) {
				editor -> handleEditorProperty(e)
				editor?.view -> handleViewProperty(e)
			}
		}

		private fun handleEditorProperty(e: PropertyChangeEvent<Any>) {
			if (e.name == Editor.PROP_CURRENT_TOOL && e.newValue == tool) {
				if (!button.isSelected) {
					button.doClick()
					button.requestFocus()
				}
			} else if (e.name == Editor.PROP_ACTIVE) {
				updateEnabledness()
			}
		}

		private fun handleViewProperty(e: PropertyChangeEvent<Any>) {
			if (e.name == View.PROP_USER_ZOOM_ENABLED || e.name == DrawingView.PROP_EDITABLE) {
				updateEnabledness()
			}
		}

		private fun updateEnabledness() {
			button.isEnabled = editor?.let {
				it.active &&  (it.view.editable || tool.enabledInUneditableView)
			} ?: false
		}
	}
}