package ch.scorpion.jabbah.base.preferences

import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.swing.EGBL
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/** Displays editors for all [Preference]s of a particular [PreferenceGroup].*/
class PreferencesPanel(
	private val group: PreferenceGroup,
	val preferences: Properties
) : JPanel() {

	init {
		buildUI()
	}

	private var gridy = 0

	private val inset = 10

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
		EGBL.add(
			this,
			row,
			0, gridy,	// x, y
			EGBL.REMAINDER , 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,	// fill
			inset, inset, 0, 0
		)
		gridy++
	}

	fun addLabeledRow(label: String, row: JComponent) {
		EGBL.add(
			this,
			JLabel("$label:"),
			0, gridy,	// x, y
			1, 1,	// width, height
			0.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,	// fill
			inset, inset, 0, inset
		)

		EGBL.add(
			this,
			row,
			1, gridy,	// x, y
			EGBL.REMAINDER, 1,	// width, height
			1.0, 0.0,	// weightX, weightY
			EGBL.WEST,	// anchor
			EGBL.NONE,	// fill
			inset, inset, 0, 0
		)

		gridy++
	}
}