package ch.scorpion.jabbah.graph.view.style

import ch.scorpion.jabbah.draw.style.BasicStyle
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.draw.style.Style
import ch.scorpion.jabbah.draw.graphics.Font
import ch.scorpion.jabbah.draw.graphics.FontImpl
import ch.scorpion.jabbah.draw.graphics.Stroke

class EdgeStyle(
	color: CompositeColor = CompositeColor(),
	stroke: Stroke = Stroke(),
	font: Font = FontImpl(),
	val busStroke: Stroke = Stroke(),
	val executionStroke: Stroke = stroke
) : BasicStyle(color, stroke, font), Style