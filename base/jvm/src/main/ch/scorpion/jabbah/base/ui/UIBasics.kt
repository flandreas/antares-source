package ch.scorpion.jabbah.base.ui

import org.apache.commons.lang3.SystemUtils
import java.awt.Component
import java.awt.Font
import javax.swing.*
import javax.swing.border.Border

object UIBasics {

	val HEADER_FONT: Font = UIManager.getFont("Label.font").let {
		it.deriveFont(Font.BOLD, it.size + 1f)!!
	}

	/** The gap between individual buttons e.g. in dialogs, such as "OK" and "Cancel".*/
	const val BUTTON_GAP = 6

	/** The gap between groups of buttons e.g. in dialogs.*/
	const val BUTTON_GROUP_GAP = 18

	/** Creates an empty border to be used in dialogs around the content.*/
	fun createDialogBorder(): Border = BorderFactory.createEmptyBorder(10, 10, 10, 10)

	fun createHeaderLabel(text: String, icon: Icon? = null): JLabel =
		JLabel(text, icon, SwingConstants.LEFT).apply { font = HEADER_FONT }

	/**
	 * Adds an affirmative button (e.g. "OK") and a dismissive button (e.g. "Cancel") to [panel]
	 * in the order following the standards of the current platform. The buttons are separated
	 * by [separator], which defaults to a horizontal strut of standard width.
	 */
	fun addButtons(
		panel: JPanel,
		affirmative: JComponent,
		dismissive: JComponent,
		separator: Component = Box.createHorizontalStrut(BUTTON_GAP)
	) {
		if (SystemUtils.IS_OS_MAC) {
			panel.add(dismissive)
			panel.add(separator)
			panel.add(affirmative)
		} else {
			panel.add(affirmative)
			panel.add(separator)
			panel.add(dismissive)
		}
	}
}