package ch.scorpion.jabbah.base.ui

import java.awt.Font
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.UIManager

object UIBasics {

	private val HEADER_FONT: Font = UIManager.getFont("Label.font").let {
		it.deriveFont(Font.BOLD, it.size + 1f)!!
	}

	fun createHeaderLabel(text: String, icon: Icon? = null): JLabel =
		JLabel(text, icon, SwingConstants.LEFT).apply { font = HEADER_FONT }

}