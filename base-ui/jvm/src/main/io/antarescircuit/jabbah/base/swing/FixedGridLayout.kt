package io.antarescircuit.jabbah.base.swing

import java.awt.Component
import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager
import kotlin.math.ceil
import kotlin.math.max

/**
 * Lays out [Components][Component] in a fixed-sized grid, all top-left aligned.
 */
class FixedGridLayout(
	private val cellSize: Int = 100,
	private val cellGap: Int = 0
) : LayoutManager {

	override fun addLayoutComponent(name: String, comp: Component) { }

	override fun removeLayoutComponent(comp: Component) { }

	override fun preferredLayoutSize(parent: Container): Dimension {
		synchronized(parent.treeLock) {
			val insets = parent.insets
			val colCount = max(1, (parent.width - insets.left - insets.right) / (cellSize + cellGap))
			val rowCount = ceil(parent.componentCount.toFloat() / colCount.toFloat()).toInt()
			return Dimension(
				insets.left + colCount * cellSize + (colCount - 1) * cellGap + insets.right,
				insets.top + rowCount * cellSize + (rowCount - 1) * cellGap + insets.bottom)
		}
	}

	override fun minimumLayoutSize(parent: Container): Dimension {
		synchronized(parent.treeLock) {
			val insets = parent.insets
			return Dimension(
				insets.left + cellSize + insets.right,
				insets.top + cellSize + insets.bottom)
		}
	}

	override fun layoutContainer(parent: Container) {
		synchronized(parent.treeLock) {
			val insets = parent.insets
			var x = insets.left
			var y = insets.top
			for (i in 0 until parent.componentCount) {
				parent.getComponent(i).setBounds(x, y, cellSize, cellSize)
				x += (cellSize + cellGap)
				if (x + cellSize >  parent.width - insets.left) {
					x = insets.left
					y += (cellSize + cellGap)
				}
			}
		}
	}
}