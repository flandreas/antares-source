package ch.scorpion.jabbah.app.rating

import ch.scorpion.jabbah.base.swing.UiUtil
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JLabel
import javax.swing.JPanel

class OverallRatingPanel : JPanel() {

	companion object {
		private val starIcon = UiUtil.themedIcon("/img/star.png")
		private val fullStarIcon = UiUtil.themedIcon("/img/star-full.png")
	}

	private val starLabels = mutableListOf<JLabel>()

	private val clickListener = object : MouseAdapter() {
		override fun mousePressed(e: MouseEvent) {
			handleClick(e.source as JLabel)
		}
	}

	/** The rating either 0 (if not yet given) or 1..5 depending on which star the user clicked. */
	var rating: Int = 0
		private set

	init {
		repeat(5) {
			val label = JLabel(starIcon)
			label.addMouseListener(clickListener)
			starLabels.add(label)
		}
		buildUI()
	}

	private fun buildUI() {
		layout = FlowLayout()
		starLabels.forEach { add(it) }

		preferredSize = minimumSize
	}

	private fun handleClick(source: JLabel) {
		rating = starLabels.indexOf(source) + 1
		updateIcons()
	}

	private fun updateIcons() {
		starLabels.forEachIndexed { index, label ->
			if (rating < index + 1) {
				label.icon = starIcon
			} else {
				label.icon = fullStarIcon
			}
		}
	}
}