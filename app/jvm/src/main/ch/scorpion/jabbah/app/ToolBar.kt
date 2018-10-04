package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.PropertyChangeEvent
import ch.scorpion.jabbah.base.event.PropertyChangeListener
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.Tool
import java.awt.event.ActionEvent
import java.awt.event.ActionListener
import javax.swing.*

/**
 * Contains a list of [JButton]s that allows the user to select an [Editor]'s current [Tool].
 */
/*
class ToolBar(val editor: Editor) : JToolBar() {

    private val buttonGroup = ButtonGroup()

    init {
        isFloatable = false
        isRollover = false
    }

    /** ---- [JComponent] */

    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        for (c in components) {
            c.isEnabled = enabled
        }
    }

    /** ---- [ToolBar] */

    fun addTool(tool: Tool, imgPath: String, tooltipText: String) {
        val button = createButton(imgPath, tooltipText)
        button.isEnabled = isEnabled
        button.addActionListener(ToolListener(tool, button))
        buttonGroup.add(button)
        add(button)
    }

    private fun createButton(imgPath: String, tooltipText: String): JToggleButton {
        val button = JToggleButton(ImageIcon(ToolBar::class.java.getResource(imgPath)))
        button.toolTipText = tooltipText
        return button
    }

    private inner class ToolListener(val tool: Tool, val button: JToggleButton) : ActionListener, PropertyChangeListener<Any> {

        init {
            editor.addPropertyChangeListener(this)
        }

        override fun actionPerformed(e: ActionEvent?) {
            editor.currentTool = tool
        }

        override fun propertyChanged(e: PropertyChangeEvent<Any>) {
            if (e.name == Editor.PROP_CURRENT_TOOL && e.newValue == tool) {
                if (!button.isSelected) {
                    button.doClick()
                    button.requestFocus()
                }
            }
        }
    }
}
*/

class ToolBar(val editor: Editor? = null) : JPanel() {

	companion object {
		private const val SEPARATOR_WIDTH = 15
	}

	var isFloatable: Boolean = false

	var isRollover: Boolean = false

	private val toolGroup = ButtonGroup()

	init {
		layout = BoxLayout(this, BoxLayout.X_AXIS)
	}

	fun addSeparator() {
		add(Box.createHorizontalStrut(SEPARATOR_WIDTH))
	}

	fun addTool(tool: Tool, imgPath: String, tooltipText: String) {
		if (editor == null) {
			throw IllegalStateException("Cannot add Tool to ToolBar without Editor")
		}
		val button = createButton(imgPath, tooltipText)
		button.isEnabled = isEnabled
		button.addActionListener(ToolListener(tool, button))
		toolGroup.add(button)
		add(button)
	}

	private fun createButton(imgPath: String, tooltipText: String): JToggleButton {
		val button = JToggleButton(ImageIcon(ToolBar::class.java.getResource(imgPath)))
		button.toolTipText = tooltipText
		return button
	}

	private inner class ToolListener(val tool: Tool, val button: JToggleButton) : ActionListener, PropertyChangeListener<Any> {

		init {
			editor?.addPropertyChangeListener(this)
		}

		override fun actionPerformed(e: ActionEvent?) {
			editor?.currentTool = tool
		}

		override fun propertyChanged(e: PropertyChangeEvent<Any>) {
			if (e.name == Editor.PROP_CURRENT_TOOL && e.newValue == tool) {
				if (!button.isSelected) {
					button.doClick()
					button.requestFocus()
				}
			}
		}
	}
}