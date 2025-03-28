package ch.scorpion.antares.view.fsm

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.AbstractRectangle
import ch.scorpion.antares.model.fsm.FSMState
import ch.scorpion.antares.model.fsm.FSMTransition
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.draw.graphics.Stroke
import ch.scorpion.jabbah.draw.module.DrawModule
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.graph.view.connect.ConnectionPointHighlight
import ch.scorpion.jabbah.graph.view.style.GraphStyleType

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