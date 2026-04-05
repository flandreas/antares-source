package io.antarescircuit.jabbah.draw.graphics

import java.awt.*
import java.awt.Color
import java.awt.Graphics2D
import java.awt.Stroke
import javax.swing.Icon

/** An [Icon] that renders a [Stroke] as a small line.*/
class StrokeIcon(
	var stroke: Stroke = BasicStroke(),
	private val foregroundColor: Color = DEF_FOREGROUND_COLOR,
	private val width: Int = DEF_WIDTH,
	private val height: Int = DEF_HEIGHT
) : Icon {

	companion object {
		private val DEF_FOREGROUND_COLOR = Color.BLACK
		private const val DEF_WIDTH = 50
		private const val DEF_HEIGHT = 10
		private const val INSET = 2
	}

	override fun paintIcon(c: Component, g: Graphics, x: Int, y: Int) {
		val g2 = g as Graphics2D
		g2.color = foregroundColor
		g2.stroke = stroke
		g2.drawLine(INSET, height / 2, width - INSET, height / 2)
	}

	override fun getIconHeight(): Int = height

	override fun getIconWidth(): Int = width
}