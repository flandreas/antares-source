package ch.scorpion.jabbah.base.swing

import ch.scorpion.jabbah.base.ui.UIBasics
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

open class DataFormPanel : JPanel() {

	companion object {
		const val INSET = 7
	}

	private var gridy = 0

	init {
		layout = EGBL.getLayout()
	}

	fun addFiller() {
		EGBL.add(
			this,
			JPanel(),
			2, gridy, // x, y
			EGBL.REMAINDER, EGBL.REMAINDER, // width, height
			1.0, 1.0, // weightX, weightY
			EGBL.NORTHWEST, // anchor
			EGBL.BOTH    // fill
		)
	}

	fun addRow(row: JComponent) {
		val topInset = if (gridy == 0) INSET else 0
		EGBL.add(
			this,
			row,
			0, gridy,	// x, y
			EGBL.REMAINDER , 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,	// fill
			topInset, INSET, UIBasics.ROW_GAP, 0
		)
		gridy++
	}

	fun addLabeledRow(label: String, row: JComponent) {
		val topInset = if (gridy == 0) INSET else 0
		EGBL.add(
			this,
			JLabel("$label:"),
			0, gridy,	// x, y
			1, 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,	// fill
			topInset, INSET, UIBasics.ROW_GAP, 0
		)

		EGBL.add(
			this,
			row,
			1, gridy,	// x, y
			EGBL.REMAINDER, 1,	// width, height
			1.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,	// fill
			topInset, UIBasics.LABEL_GAP, UIBasics.ROW_GAP, 0
		)

		gridy++
	}
}