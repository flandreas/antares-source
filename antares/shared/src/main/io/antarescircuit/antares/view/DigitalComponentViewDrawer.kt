package io.antarescircuit.antares.view

import io.antarescircuit.antares.view.style.AntaresTheme
import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.drawable.AbstractDrawableDrawer
import io.antarescircuit.jabbah.draw.drawable.DrawableDrawer
import io.antarescircuit.jabbah.draw.style.Themes
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.SelectionModel
import io.antarescircuit.jabbah.graph.view.GraphElementView
import io.antarescircuit.jabbah.graph.view.style.GraphTheme

/**
 * Establish the default [Color] for drawing [io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView]s.
 *
 * The default policy for the drawing logic in [io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView]s is that they don't establish the default
 * color in which they draw themselves. This is needed for the replacement [SelectionModel] that establishes the
 * selection [Color] prior to asking the [io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView] to draw itself. As a consequence, drawing
 * unselected [io.antarescircuit.jabbah.graph.view.OrientableRectangularVerticeView]s must also be preceded by establishing the default drawing color.
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