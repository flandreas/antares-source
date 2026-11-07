package io.antarescircuit.jabbah.base.swing

import io.antarescircuit.jabbah.base.ui.UIBasics
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

open class DataFormPanel(
	val topInset: Int = DEF_INSET,
	val leftInset: Int = DEF_INSET
) : JPanel() {

	companion object {
		const val DEF_INSET = 7
	}

	private var gridy = 0

	private val rowTopInset: Int get() = if (gridy == 0) topInset else 0

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

	fun addRow(row: JComponent, fill: Boolean = false) {
		EGBL.add(
			this,
			row,
			0, gridy,	// x, y
			EGBL.REMAINDER , 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			if (fill) EGBL.HORIZONTAL else EGBL.NONE,	// fill
			rowTopInset, leftInset, UIBasics.ROW_GAP, 0
		)
		gridy++
	}

	fun addLabeledRow(label: String, row: JComponent, fill: Boolean = false) {
		EGBL.add(
			this,
			JLabel("$label:"),
			0, gridy,	// x, y
			1, 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,
			rowTopInset, leftInset, UIBasics.ROW_GAP, 0
		)

		EGBL.add(
			this,
			row,
			1, gridy,	// x, y
			EGBL.REMAINDER, 1,	// width, height
			1.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			if (fill) EGBL.HORIZONTAL else EGBL.NONE,	// fill
			rowTopInset, UIBasics.LABEL_GAP, UIBasics.ROW_GAP, 0
		)

		gridy++
	}
}