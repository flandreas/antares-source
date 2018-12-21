package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.Dimension
import java.awt.Frame
import javax.swing.*
import javax.swing.text.JTextComponent

/** A [JPanel] for editing a [TranslatableText] in the systems current [Language] and the default [Language].*/
class TranslatableTextPanel(
	private val textName: String,
	text: TranslatableText,
	textFieldRows: Int = 1,
	textFieldColumns: Int = 25
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
			textFieldRows: Int = 1,
			textFieldColumns: Int = 25
		) : TranslatableText? {
			val panel = TranslatableTextPanel(title, text, textFieldRows, textFieldColumns)
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

	private val currentLangTextField: JTextComponent

	private val defaultLangTextField: JTextComponent

	private val currentLanguage: Language get() = System.get().currentLanguage()

	private val isNonDefaultLanguage: Boolean get() = currentLanguage != Language.DEFAULT

	init {
		if (textFieldRows == 1) {
			currentLangTextField = JTextField()
			currentLangTextField.columns = textFieldColumns
			defaultLangTextField = JTextField()
			defaultLangTextField.columns = textFieldColumns
		} else {
			currentLangTextField = JTextArea()
			currentLangTextField.rows = textFieldRows
			currentLangTextField.columns = textFieldColumns
			currentLangTextField.lineWrap = true
			currentLangTextField.wrapStyleWord = true
			currentLangTextField.isEditable = true
			defaultLangTextField = JTextArea()
			defaultLangTextField.rows = textFieldRows
			defaultLangTextField.columns = textFieldColumns
			defaultLangTextField.lineWrap = true
			defaultLangTextField.wrapStyleWord = true
			defaultLangTextField.isEditable = true
		}

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
		var text = TranslatableText()
		if (!StringUtils.isBlank(currentLangTextField.text)) {
			text = text.withTranslation(currentLanguage, currentLangTextField.text)
		}
		if (!StringUtils.isBlank(defaultLangTextField.text)) {
			text = text.withTranslation(Language.DEFAULT, defaultLangTextField.text)
		}
		return  text
	}

	private fun buildUI(textFieldColumns: Int) {
		val inset = 5
		layout = EGBL.getLayout()

		EGBL.add(
			this,
			JLabel("$textName (${currentLanguage}):"),
			0, 0,	// x, y
			1, 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,	// fill
			0, inset, 0, 0
		)

		EGBL.add(
			this,
			if (currentLangTextField is JTextArea) UiUtil.decorateTextArea(currentLangTextField) else currentLangTextField,
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
				JLabel("$textName (${Language.DEFAULT}):"),
				0, 1,	// x, y
				1, 1,	// width, height
				0.0, 0.0,	// weightX, weightY
				EGBL.WEST,	// anchor
				EGBL.NONE,	// fill
				inset, inset, 0, 0
			)

			EGBL.add(
				this,
				if (defaultLangTextField is JTextArea) UiUtil.decorateTextArea(defaultLangTextField) else defaultLangTextField,
				1, 1,    // x, y
				EGBL.REMAINDER, 1,    // width, height
				0.0, 0.0,    // weightX, weightY
				EGBL.WEST,    // anchor
				EGBL.HORIZONTAL,    // fill
				inset, inset, 0, 0
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