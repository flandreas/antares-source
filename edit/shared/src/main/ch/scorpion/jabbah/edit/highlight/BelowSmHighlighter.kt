package ch.scorpion.jabbah.edit.highlight

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.draw.Drawable
import ch.scorpion.jabbah.draw.drawable.Colorable
import ch.scorpion.jabbah.draw.graphics.CompositeColor

/**
 * An implementation of a [Highlighter] that uses the configured [SelectionModel]s of
 * [SelectionDrawingStrategy.BELOW] for highlighting.
 */
class BelowSmHighlighter(
    private val selectionModelProvider: SelectionModelProvider = EditSelectModule.selectionModelProvider,
    private val eventBus: EventBus = BaseModule.eventBus,
    private val content: DrawingViewContent<*>
) : Highlighter {

    companion object {
        private val LOG by logger(BelowSmHighlighter::class)
    }

    /** Maps a highlighted [Component]s to its highlight [SelectionModel].*/
    private val highlights: MutableMap<Component, SelectionModel<Component>> by lazy { mutableMapOf<Component, SelectionModel<Component>>() }

    /** ---- [Highlighter] interface */

    override val highlightCount: Int
        get() = highlights.size

    override fun highlight(component: Component, color: CompositeColor?) {
        if (!isHighlighted(component)) {
            highlightImpl(component, color)
            content.highlightContainer.validate()
            postHighlightChangedEvent(listOf(component), true)
        }
    }

    override fun highlight(components: Collection<Component>) {
        val newHighlights = mutableListOf<Component>()
        components
            .filter { !isHighlighted(it) }
            .forEach {
                highlightImpl(it)
                newHighlights.add(it)
            }
        if (newHighlights.size > 0) {
            content.highlightContainer.validate()
            postHighlightChangedEvent(newHighlights, true)
        }
    }

    override fun highlight(vararg ids: Int) {
        highlight(content.drawing.getDrawables { ids.contains(it.id) })
    }

    override fun unhighlight(component: Component) {
        if (isHighlighted(component)) {
            unhighlightImpl(component)
            content.highlightContainer.validate()
            postHighlightChangedEvent(listOf(component), false)
        }
    }

    override fun unhighlight(components: Collection<Component>) {
        val oldHighlights = mutableListOf<Component>()
        components
                .filter { isHighlighted(it) }
                .forEach {
                    unhighlightImpl(it)
                    oldHighlights.add(it)
                }
        if (oldHighlights.size > 0) {
            content.highlightContainer.validate()
            postHighlightChangedEvent(oldHighlights, false)
        }
    }

    override fun unhighlight(vararg ids: Int) {
        unhighlight(content.drawing.getDrawables { ids.contains(it.id) })
    }

    override fun unhighlightAll() {
        val list = mutableListOf<Component>()
        list.addAll(highlights.keys)
        if (!list.isEmpty()) {
            list.forEach { unhighlightImpl(it) }
            content.highlightContainer.validate()
            postHighlightChangedEvent(list, false)
        }
    }

    override fun isHighlighted(component: Component): Boolean = highlights.containsKey(component)

    override fun replaceColor(oldColor: CompositeColor, newColor: CompositeColor) {
        highlights.values
                .filter { it is Colorable && it.color == oldColor }
                .map { it as Colorable }
                .forEach { it.color = newColor }
    }

	override fun getHighlightFor(component: Component): Drawable? {
		return highlights[component]
	}

    /** ---- [BelowSmHighlighter] */

    private fun highlightImpl(c: Component, color: CompositeColor? = null) {
        LOG.debug("Highlight component '${c.id}'")
        val highlight = selectionModelProvider.provideFor(c, SelectionDrawingStrategy.BELOW)
        if (highlight == null) {
            LOG.error("No suitable highlight SelectionModel found for ${System.get().getClassName(c)}")
            return
        }
        if (color != null) {
            if (highlight is Colorable) {
                highlight.color = color
            } else {
                LOG.warn("BelowSmHighlighter: requested highlight color for non-colorable SelectionModel ${highlight::class.simpleName}")
            }
        }
        highlights.put(c, highlight)
        content.highlightContainer.add(highlight)
    }

    private fun unhighlightImpl(c: Component) {
        LOG.debug("Unhighlight component '${c.id}'")
        val highlight = highlights[c]
        highlights.remove(c)
        content.highlightContainer.remove(highlight!!)
    }

    private fun postHighlightChangedEvent(c: Collection<Component>, highlighted: Boolean) {
        eventBus.post(HighlightChangeEvent(
            highlighter = this,
            content = content,
            components = c,
            highlighted = highlighted))
    }
}