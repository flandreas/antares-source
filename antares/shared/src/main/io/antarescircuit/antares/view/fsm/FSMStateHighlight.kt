package io.antarescircuit.antares.view.fsm

import io.antarescircuit.jabbah.draw.Drawable
import io.antarescircuit.jabbah.draw.drawable.AbstractRectangle
import io.antarescircuit.antares.model.fsm.FSMState
import io.antarescircuit.antares.model.fsm.FSMTransition
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Stroke
import io.antarescircuit.jabbah.draw.module.DrawModule
import io.antarescircuit.jabbah.draw.style.DrawStyleModule
import io.antarescircuit.jabbah.graph.view.connect.highlight.ConnectionPointHighlight
import io.antarescircuit.jabbah.graph.view.style.GraphStyleType

/**
 * A [Drawable] used to highlight a [FSMState] where a new [FSMTransition] could start or end.
 * Used when interactively creating [FSMTransition]s.
 */
class FSMStateHighlight : AbstractRectangle() {

    companion object {
        private val STROKE = Stroke(DrawStyleModule.styleProvider.getStyle(GraphStyleType.EDGE).stroke.width)

        private const val DIST = 3
    }

    override val lineWidth: Double get() = STROKE.width.toDouble()

    override fun draw(context: DrawContext) {
        context.g.color = DrawModule.properties.getColor(ConnectionPointHighlight.PROP_COLOR)
        context.g.stroke = STROKE
        context.g.drawOval(x, y, width, height)
    }

    fun updateForState(state : FSMState) {
        setBounds(state.x - DIST, state.y - DIST, state.width + 2 * DIST, state.height + 2 * DIST)
    }
}