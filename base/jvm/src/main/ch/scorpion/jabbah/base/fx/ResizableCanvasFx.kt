package ch.scorpion.jabbah.base.fx

import javafx.scene.canvas.Canvas

class ResizableCanvasFx : Canvas() {

	var repaintCallback: () -> Unit = {}

	override fun minHeight(width: Double): Double {
		return 64.0
	}

	override fun maxHeight(width: Double): Double {
		return 2000.0
	}

	override fun prefHeight(width: Double): Double {
		return minHeight(width)
	}

	override fun minWidth(height: Double): Double {
		return 0.0
	}

	override fun maxWidth(height: Double): Double {
		return 2000.0
	}

	override fun isResizable(): Boolean = true

	override fun resize(width: Double, height: Double) {
		super.setWidth(width)
		super.setHeight(height)
		repaintCallback.invoke()
	}
}