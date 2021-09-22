package ch.scorpion.jabbah.base.swing

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTextArea
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener

/**
 * A scrollable [JTextArea] that displays line numbers in the [JScrollPane]'s row header.
 */
class LineNumberTextArea(
	editable: Boolean = true,
	text: String = "",
	font: Font = FONT
) : JPanel() {

	companion object {
		private val FONT = Font(Font.MONOSPACED, Font.PLAIN, 12)
	}

	val mainTextArea = JTextArea(text)
	private val lineNumberTextArea = JTextArea(getLineNumbers2())
	private val scrollPane = JScrollPane()

	var text: String
		get() = mainTextArea.text!!
		set(value) {
			mainTextArea.text = value
		}

	init {
		buildUI()

		mainTextArea.wrapStyleWord = true
		mainTextArea.lineWrap = true
		mainTextArea.wrapStyleWord = true
		mainTextArea.isEditable = editable
		mainTextArea.font = font
		mainTextArea.tabSize = 4
		mainTextArea.document.addDocumentListener(TextListener())

		lineNumberTextArea.isEditable = false
		lineNumberTextArea.isEnabled = false
		lineNumberTextArea.columns = 4
		lineNumberTextArea.font = font
	}

	private fun buildUI() {
		layout = BorderLayout(0, 0)

		scrollPane.setViewportView(mainTextArea)
		scrollPane.setRowHeaderView(lineNumberTextArea)
		scrollPane.horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_NEVER
		scrollPane.verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
		scrollPane.preferredSize = Dimension(Int.MAX_VALUE, Int.MAX_VALUE)

		add(scrollPane, BorderLayout.CENTER)
	}

	private fun updateLineNumbers() {
		lineNumberTextArea.text = getLineNumbers2()
	}

	private fun getLineNumbers2(): String {
		val text = StringBuilder()
		for (i in 1..mainTextArea.lineCount) {
			text.append("$i\n")
		}
		return text.toString()
	}

	private inner class TextListener : DocumentListener {
		override fun insertUpdate(e: DocumentEvent?) { updateLineNumbers() }
		override fun removeUpdate(e: DocumentEvent?) { updateLineNumbers() }
		override fun changedUpdate(e: DocumentEvent?) { updateLineNumbers() }
	}
}