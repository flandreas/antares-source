package ch.scorpion.antares.view

import ch.scorpion.antares.view.style.AntaresTheme
import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.drawable.AbstractDrawableDrawer
import ch.scorpion.jabbah.draw.drawable.DrawableDrawer
import ch.scorpion.jabbah.draw.style.Themes
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.SelectionModel
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.style.GraphTheme

/**
 * Establish the default [Color] for drawing [OrientableRectangularVerticeView]s.
 *
 * The default policy for the drawing logic in [OrientableRectangularVerticeView]s is that they don't establish the default
 * color in which they draw themselves. This is needed for the replacement [SelectionModel] that establishes the
 * selection [Color] prior to asking the [OrientableRectangularVerticeView] to draw itself. As a consequence, drawing
 * unselected [OrientableRectangularVerticeView]s must also be preceded by establishing the default drawing color.
 */
class DigitalComponentViewDrawer(successor: DrawableDrawer<Component>?) : AbstractDrawableDrawer<Component>() {
	constructor() : this(null)

	init {
		successor?.let { super.successor = successor }
	}

	override fun process(context: DrawContext, drawable: Component) {
		val oldUseContextColor = context.useContextColors
		if (drawable is GraphElementView<*> && drawable.model.isError) {
			context.useContextColors = true
			context.g.color = Themes.get<AntaresTheme>().error.foregroundColor
			context.color = Themes.get<AntaresTheme>().error
		} else {
			context.g.color = Themes.get<GraphTheme>().vertice.color.foregroundColor
			context.color = Themes.get<GraphTheme>().vertice.color
			context.selectionColor = Themes.get<GraphTheme>().selection.color
		}
		context.g.stroke = Themes.get<GraphTheme>().edge.stroke
		nextProcessor(context, drawable)

		context.useContextColors = oldUseContextColor
	}
}