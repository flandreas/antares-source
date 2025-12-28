package ch.scorpion.jabbah.base.swing

import java.awt.Container
import java.awt.Dimension
import java.awt.LayoutManager

class FillAllLayout : LayoutManager {

    companion object {
        private val MAX_DIMENSION = Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)
    }

    override fun addLayoutComponent(name: String?, comp: java.awt.Component?) {}

    override fun removeLayoutComponent(comp: java.awt.Component?) {}

    override fun preferredLayoutSize(parent: Container): Dimension {
        synchronized(parent.treeLock) {
            return MAX_DIMENSION
        }
    }

    override fun minimumLayoutSize(parent: Container): Dimension {
        synchronized(parent.treeLock) {
            return MAX_DIMENSION
        }
    }

    override fun layoutContainer(parent: Container) {
        synchronized(parent.treeLock) {
            val b = parent.bounds
            for (i in 0 until parent.componentCount) {
                parent.getComponent(i).setBounds(0, 0, b.width, b.height)
            }
        }
    }
}