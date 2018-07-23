package ch.scorpion.jabbah.edit

import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.graphics.CompositeColor

/**
 * Manages the highlighting of [Component]s in a [Drawing].
 *
 * Other than the concept of "selection", which selects [Component]s in order to be changed by the user,
 * "highlighting" is a concept that can also be used in the absence of an [Editor], for example for
 * guiding the user's attention to a particular [Component] during a simulation.
 */
interface Highlighter {

    /** Holds the number of currently highlighted [Component]s.*/
    val highlightCount: Int

    /** Highlights the specified [Component].*/
    fun highlight(component: Component, color: CompositeColor? = null)

    /** Highlights all specified [Component]s.*/
    fun highlight(components: Collection<Component>)

    /** Highlights the [Component]s with the specified IDs.*/
    fun highlight(vararg ids: Int)

    /** Unhighlights the specified [Component].*/
    fun unhighlight(component: Component)

    /** Unhighlights the specified [Component]s. */
    fun unhighlight(components: Collection<Component>)

    /** Unhighlights the [Component]s with the specified IDs.*/
    fun unhighlight(vararg ids: Int)

    /** Unhighlights all currently highlighted [Component]s.*/
    fun unhighlightAll()

    /** Determines whether a particular [Component] is currently highlighted.*/
    fun isHighlighted(component: Component): Boolean

    /** Replace a given highlight [CompositeColor] by a corresponding new one. */
    fun replaceColor(oldColor: CompositeColor, newColor: CompositeColor)

	fun getHighlightFor(component: Component): Drawable?
}

interface HighlighterFactory {

    /** Creates a [Highlighter] that manages highlights of [Component]s in the specified [DrawingViewContent].*/
    fun create(content: DrawingViewContent<*>): Highlighter
}