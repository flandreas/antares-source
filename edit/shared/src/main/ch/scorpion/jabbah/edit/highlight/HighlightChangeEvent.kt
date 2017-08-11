package ch.scorpion.jabbah.edit.highlight

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.*

/**
 * Posted by a [Highlighter] on its [EventBus] whenever the current highlighting in a [DrawingView] has changed.
 */
data class HighlightChangeEvent(
    val content: DrawingViewContent<*>,
    val highlighter: Highlighter,
    val type: Type,
    val components: Collection<Component>
) {
    constructor(content: DrawingViewContent<*>, highlighter: Highlighter, components: Collection<Component>, highlighted: Boolean):
        this(content, highlighter, if(highlighted) Type.HIGHLIGHTED else Type.UNHIGHLIGHTED, components)

    enum class Type {
        HIGHLIGHTED,
        UNHIGHLIGHTED
    }

    val highlighted: Boolean = type == Type.HIGHLIGHTED
    val unhighlighted: Boolean = type == Type.UNHIGHLIGHTED
}