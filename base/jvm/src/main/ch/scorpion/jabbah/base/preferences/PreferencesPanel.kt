package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.swing.EGBL
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/** Displays editors for all [Preference]s of a particular [PreferenceGroup].*/
class PreferencesPanel(
	private val group: PreferenceGroup,
	val preferences: Preferences
) : JPanel() {

	companion object {
		private const val INSET = 7
		private const val ROW_GAP = 5
		private const val LABEL_GAP = 5
	}

	init {
		buildUI()
	}

	private var gridy = 0

	private fun buildUI() {
		layout = EGBL.getLayout()

		for (preference in group.preferences) {
			preference.addToPanel(this)
		}

		// filler
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

	fun load() {
		for (preference in group.preferences) {
			preference.load()
		}
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
			topInset, INSET, ROW_GAP, 0
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
			topInset, INSET, ROW_GAP, 0
		)

		EGBL.add(
			this,
			row,
			1, gridy,	// x, y
			EGBL.REMAINDER, 1,	// width, height
			1.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,	// fill
			topInset, LABEL_GAP, ROW_GAP, 0
		)

		gridy++
	}
}