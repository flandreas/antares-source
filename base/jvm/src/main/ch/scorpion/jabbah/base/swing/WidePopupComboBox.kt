package ch.scorpion.jabbah.base.swing

import java.awt.Dimension
import javax.swing.JComboBox
import javax.swing.UIManager
import kotlin.math.max

/**
 * A [JComboBox] whose width is determined by a "prototype display value", and the width of the
 * popup menu is determined by the widest item, which is possibly wider than the [JComboBox].
 *
 * Source: https://nicolas.riousset.com/category/software-methodologies/how-to-resize-the-width-of-a-jcombobox-dropdown-list-without-resizing-the-edit-box/
 */
open class WidePopupComboBox<T>(
	prototypeDisplayValue: T,
	private val rightInset:Int = 5
) : JComboBox<T>() {

	private var popupMenuWidth: Int = 0
	private var layingOut = false

	init {
		this.prototypeDisplayValue = prototypeDisplayValue
	}

	fun adjustPopupWidth() {
		popupMenuWidth = computeMaxItemWidth()
	}

	private fun computeMaxItemWidth(): Int {
		val numOfItems = itemCount
		val font = this.font
		val fontMetrics = getFontMetrics(font)

		var widest: Int = 0
		for (i in 0 until numOfItems) {
			val item = getItemAt(i)
			val lineWidth = fontMetrics.stringWidth(item.toString()) + rightInset
			widest = max(widest, lineWidth)
		}

		val scrollbarWidth = UIManager.getInt("ScrollBar.width")
		return max(widest + scrollbarWidth, preferredSize.width)
	}

	override fun doLayout() {
		try {
			layingOut = true
			super.doLayout()
		} finally {
			layingOut = false
		}
	}

	override fun getSize(): Dimension {
		val dim = super.getSize()
		if (!layingOut) {
			dim.width = max(popupMenuWidth, dim.width)
		}
		return dim
	}
}