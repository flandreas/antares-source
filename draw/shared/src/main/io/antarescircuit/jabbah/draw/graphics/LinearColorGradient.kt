package io.antarescircuit.jabbah.draw.graphics

import io.antarescircuit.jabbah.base.geom.Point2D

/**
 * Used as wrapper object in [Graphics2D] to abstract from platform-specific color gradients.
 */
data class LinearColorGradient(
	val p1: Point2D,
	val color1: Color,
	val p2: Point2D,
	val color2: Color
) : Paint