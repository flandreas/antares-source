package io.antarescircuit.jabbah.graph.view.style

import io.antarescircuit.jabbah.draw.style.BasicStyle
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.draw.style.Style
import io.antarescircuit.jabbah.draw.graphics.Font
import io.antarescircuit.jabbah.draw.graphics.FontImpl
import io.antarescircuit.jabbah.draw.graphics.Stroke

class EdgeStyle(
	color: CompositeColor = CompositeColor(),
	stroke: Stroke = Stroke(),
	font: Font = FontImpl(),
	val busStroke: Stroke = Stroke(),
	val executionStroke: Stroke = stroke
) : BasicStyle(color, stroke, font), Style