package ch.scorpion.jabbah.draw.graphics

import ch.scorpion.jabbah.base.geom.Point2D

data class LinearColorGradient(
	val p1: Point2D,
	val color1: Color,
	val p2: Point2D,
	val color2: Color) : Paint