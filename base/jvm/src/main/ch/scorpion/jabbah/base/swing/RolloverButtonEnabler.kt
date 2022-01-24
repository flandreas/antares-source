package ch.scorpion.jabbah.base.swing;

import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.AbstractButton
import javax.swing.JComponent
import javax.swing.event.ChangeEvent
import javax.swing.event.ChangeListener

class RolloverButtonEnabler(private val button: AbstractButton) : MouseAdapter(), ChangeListener {

	init {
		button.isRolloverEnabled = true
		button.isOpaque = true
		button.background = null
		button.model.addChangeListener(this)
		button.addMouseListener(this)
	}

	override fun stateChanged(e: ChangeEvent?) {
		setColorDependingOnRolloverState()
	}

	private fun setColorDependingOnRolloverState() {
		if (button.model.isRollover) {
			button.background = UiUtil.getBackgroundDivertColor(button.parent!! as JComponent)
		} else {
			button.background = null
		}
	}

	override fun mousePressed(e: MouseEvent?) {
		button.background = UiUtil.getButtonPressColor(button.parent!! as JComponent)
	}

	override fun mouseReleased(e: MouseEvent?) {
		setColorDependingOnRolloverState()
	}
}