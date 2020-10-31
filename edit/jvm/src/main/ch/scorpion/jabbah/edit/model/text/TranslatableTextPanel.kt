package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.swing.EGBL
import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.Component
import java.awt.Frame
import javax.swing.*
import javax.swing.text.JTextComponent

/**
 * A [JPanel] for editing a [TranslatableText] in the systems current [Language] and the default [Language].
 * If the [TranslatableText] doesn't contain a text for both of these [Language]s, any available [Language]
 * is used.
 */
class TranslatableTextPanel(
	text: TranslatableText,
	textFieldRows: Int = 1,
	textFieldColumns: Int = 25,
	editable: Boolean = true
) : JPanel() {

	companion object {

		/**
		 * Shows [TranslatableTextPanel] as a dialog for editing the specified [TranslatableText]
		 * @return the edited [TranslatableText], or `null` if the dialog has been cancelled by the user
		 */
		fun showAsDialog(
			parent: Component = Frame.getFrames()[0],
			title: String,
			text: TranslatableText,
			textFieldRows: Int = 1,
			textFieldColumns: Int = 25,
			editable: Boolean = true
		): TranslatableText? {
			val panel = TranslatableTextPanel(text, textFieldRows, textFieldColumns, editable)
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

	val currentLangTextField: JTextComponent

	val alternativeLangTextField: JTextComponent

	private val currentLanguage: Language = System.currentLanguage()

	private val needsAlternativeLangText: Boolean = currentLanguage.isNonDefault || (!text.hasDefaultOrSystemLanguage() && !text.isEmpty)

	private val alternativeLanguage: Language?

	init {
		if (textFieldRows == 1) {
			currentLangTextField = JTextField()
			currentLangTextField.columns = textFieldColumns
			alternativeLangTextField = JTextField()
			alternativeLangTextField.columns = textFieldColumns
		} else {
			currentLangTextField = JTextArea()
			currentLangTextField.rows = textFieldRows
			currentLangTextField.columns = textFieldColumns
			currentLangTextField.lineWrap = true
			currentLangTextField.wrapStyleWord = true

			alternativeLangTextField = JTextArea()
			alternativeLangTextField.rows = textFieldRows
			alternativeLangTextField.columns = textFieldColumns
			alternativeLangTextField.lineWrap = true
			alternativeLangTextField.wrapStyleWord = true
		}

		currentLangTextField.isEditable = editable
		alternativeLangTextField.isEditable = editable

		alternativeLanguage = if (needsAlternativeLangText) {
			if (!text.hasDefaultOrSystemLanguage()) {
				text.getFirstLanguage() ?: Language.DEFAULT
			} else {
				Language.DEFAULT
			}
		} else {
			null
		}

		if (text.hasTranslation(currentLanguage)) {
			currentLangTextField.text = text.getTranslation(currentLanguage)
		}
		if (needsAlternativeLangText) {
			if (text.hasTranslation(alternativeLanguage!!)) {
				alternativeLangTextField.text = text.getTranslation(alternativeLanguage)
			}
		}

		buildUI()

		// Due to a strange behaviour of JTextArea, longs texts that would need the JScrollPane to display the
		// scrollbar result in tiny JTextArea heights.
		// See https://stackoverflow.com/questions/455753/jtextarea-very-small-size-with-long-text.
		revalidate()
		currentLangTextField.size = currentLangTextField.preferredSize
		alternativeLangTextField.size = alternativeLangTextField.preferredSize
		revalidate()
	}

	private val text: TranslatableText
		get() {
			var text = TranslatableText()
			if (!StringUtils.isBlank(currentLangTextField.text)) {
				text = text.withTranslation(currentLanguage, currentLangTextField.text)
			}
			if (needsAlternativeLangText && !StringUtils.isBlank(alternativeLangTextField.text)) {
				text = text.withTranslation(alternativeLanguage!!, alternativeLangTextField.text)
			}
			return text
		}

	private fun buildUI() {
		val inset = 5
		layout = EGBL.getLayout()

		EGBL.add(
			this,
			JLabel("$currentLanguage:"),
			0, 0,    // x, y
			1, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.NORTHWEST,    // anchor
			EGBL.NONE,    // fill
			currentLangTextField.insets.top, inset, 0, 0
		)

		EGBL.add(
			this,
			if (currentLangTextField is JTextArea) UiUtil.decorateTextArea(currentLangTextField) else currentLangTextField,
			1, 0,    // x, y
			EGBL.REMAINDER, 1,    // width, height
			0.0, 0.0,    // weightX, weightY
			EGBL.NORTHWEST,    // anchor
			EGBL.HORIZONTAL,    // fill
			0, inset, 0, 0
		)

		if (needsAlternativeLangText) {
			EGBL.add(
				this,
				JLabel("$alternativeLanguage:"),
				0, 1,    // x, y
				1, 1,    // width, height
				0.0, 0.0,    // weightX, weightY
				EGBL.NORTHWEST,    // anchor
				EGBL.NONE,    // fill
				alternativeLangTextField.insets.top + inset, inset, 0, 0
			)

			EGBL.add(
				this,
				if (alternativeLangTextField is JTextArea) UiUtil.decorateTextArea(alternativeLangTextField) else alternativeLangTextField,
				1, 1,    // x, y
				EGBL.REMAINDER, 1,    // width, height
				0.0, 0.0,    // weightX, weightY
				EGBL.NORTHWEST,    // anchor
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