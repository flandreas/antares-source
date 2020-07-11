package ch.scorpion.antares.view.gate

import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Color

interface CustomShapeContent {

	fun drawCustomShapeContent(context: DrawContext, foregroundColor: Color, backgroundColor: Color)
}