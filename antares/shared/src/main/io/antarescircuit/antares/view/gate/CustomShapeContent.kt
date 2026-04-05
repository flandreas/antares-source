package io.antarescircuit.antares.view.gate

import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Color

interface CustomShapeContent {

	fun drawCustomShapeContent(context: DrawContext, foregroundColor: Color, backgroundColor: Color)
}