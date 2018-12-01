package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.EGBL
import java.awt.Frame
import javax.swing.JLabel
import javax.swing.JOptionPane
import javax.swing.JPanel
import javax.swing.JTextField

/** A [JPanel] for editing a [TranslatableText] in the systems current [Language] and the default [Language].*/
class TranslatableTextPanel(
	text: TranslatableText,
	textFieldColumns: Int = 20
) : JPanel() {

	companion object {

		/**
		 * Shows [TranslatableTextPanel] as a dialog for editing the specified [TranslatableText]
		 * @return the edited [TranslatableText], or `null` if the dialog has been cancelled by the user
		 */
		fun showAsDialog(
			parent: Frame = Frame.getFrames()[0],
			title: String,
			text: TranslatableText,
			textFieldColumns: Int = 20
		) : TranslatableText? {
			val panel = TranslatableTextPanel(text, textFieldColumns)
			return when (
			JOptionPane.showConfirmDialog(
				parent,
				panel,
				title,
				JOptionPane.OK_CANCEL_OPTION,
				JOptionPane.PLAIN_MESSAGE)
			) {
				JOptionPane.OK_OPTION -> panel.text
				else -> null
			}
		}
	}

	private val currentLangTextField = JTextField()

	private val defaultLangTextField = JTextField()

	private val currentLanguage: Language get() = System.get().currentLanguage()

	private val isNonDefaultLanguage: Boolean get() = currentLanguage != Language.DEFAULT

	init {
		if (text.hasTranslation(currentLanguage)) {
			currentLangTextField.text = text.getTranslation(currentLanguage)
		}
		if (isNonDefaultLanguage) {
			if (text.hasTranslation(Language.DEFAULT)) {
				defaultLangTextField.text = text.getTranslation(Language.DEFAULT)
			}
		}
		buildUI(textFieldColumns)
	}

	private val text: TranslatableText get() {
		val text = TranslatableText()
		if (!StringUtils.isBlank(currentLangTextField.text)) {
			text.setTranslation(currentLanguage, currentLangTextField.text)
		}
		if (!StringUtils.isBlank(defaultLangTextField.text)) {
			text.setTranslation(Language.DEFAULT, defaultLangTextField.text)
		}
		return  text
	}

	private fun buildUI(textFieldColumns: Int) {
		val inset = 5
		layout = EGBL.getLayout()

		EGBL.add(
			this,
			JLabel(Translations.getString("directory.property.name.name", currentLanguage.toString()) + ":"),
			0, 0,	// x, y
			1, 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,	// fill
			0, inset, 0, 0
		)

		currentLangTextField.columns = textFieldColumns
		EGBL.add(
			this,
			currentLangTextField,
			1, 0,	// x, y
			EGBL.REMAINDER, 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.HORIZONTAL,	// fill
			0, inset, 0, 0
		)

		if (isNonDefaultLanguage) {
			EGBL.add(
				this,
				JLabel(Translations.getString("directory.property.name.name", Language.DEFAULT.toString()) + ":"),
				0, 1,	// x, y
				1, 1,	// width, height
				0.0, 0.0,	// weightX, weightY
				EGBL.WEST,	// anchor
				EGBL.NONE,	// fill
				0, inset, 0, 0
			)

			defaultLangTextField.columns = textFieldColumns
			EGBL.add(
				this,
				defaultLangTextField,
				1, 1,    // x, y
				EGBL.REMAINDER, 1,    // width, height
				0.0, 0.0,    // weightX, weightY
				EGBL.WEST,    // anchor
				EGBL.HORIZONTAL,    // fill
				0, inset, 0, 0
			)
		}

		// filler
		EGBL.add(
			this,
			JPanel(),
			10, 10, // x, y
			EGBL.REMAINDER, EGBL.REMAINDER, // width, height
			1.0, 1.0, // weightX, weightY
			EGBL.NORTHWEST, // anchor
			EGBL.BOTH    // fill
		)
	}
}