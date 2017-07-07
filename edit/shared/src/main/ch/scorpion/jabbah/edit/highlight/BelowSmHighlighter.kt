package ch.scorpion.jabbah.edit.highlight

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.select.EditSelectModule
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.logger

/**
 * An implementation of a [Highlighter] that uses the configured [SelectionModel]s of
 * [SelectionDrawingStrategy.BELOW] for highlighting.
 */
class BelowSmHighlighter(
    private val selectionModelProvider: SelectionModelProvider,
    private val eventBus: EventBus,
    private val drawingView: DrawingView<out Drawing<Component>>
) : Highlighter {

    constructor(drawingView: DrawingView<out Drawing<Component>>)
        : this(EditSelectModule.selectionModelProvider, BaseModule.eventBus, drawingView)

    /** Maps a highlighted [Component]s to its highlight [SelectionModel].*/
    private val highlights: MutableMap<Component, SelectionModel<Component>> by lazy { mutableMapOf<Component, SelectionModel<Component>>() }

    private val LOG by logger()

    /** ---- [Highlighter] interface */

    override val highlightCount: Int
        get() = highlights.size

    override fun highlight(component: Component) {
        if (!isHighlighted(component)) {
            highlightImpl(component)
            drawingView.drawing.validate()
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
            drawingView.drawing.validate()
            postHighlightChangedEvent(newHighlights, true)
        }
    }

    override fun highlight(vararg ids: Int) {
        highlight(drawingView.drawing.getDrawables { ids.contains(it.id) })
    }

    override fun unhighlight(component: Component) {
        if (isHighlighted(component)) {
            unhighlightImpl(component)
            drawingView.drawing.validate()
            postHighlightChangedEvent(listOf(component), false)
        }
    }

    override fun unhighlightAll() {
        val list = mutableListOf<Component>()
        list.addAll(highlights.keys)
        if (!list.isEmpty()) {
            list.forEach { unhighlightImpl(it) }
            drawingView.drawing.validate()
            postHighlightChangedEvent(list, false)
        }
    }

    override fun isHighlighted(component: Component): Boolean {
        return highlights.containsKey(component)
    }

    /** ---- [BelowSmHighlighter] */

    private fun highlightImpl(c: Component) {
        LOG.debug("Highlight component '${c.id}'")
        val highlight = selectionModelProvider.provideFor(c, SelectionDrawingStrategy.BELOW)
        if (highlight == null) {
            LOG.error("No suitable highlight SelectionModel found for ${System.get().getClassName(c)}")
            return
        }
        highlights.put(c, highlight)
        drawingView.highlightContainer.add(highlight)
    }

    private fun unhighlightImpl(c: Component) {
        LOG.debug("Unhighlight component '${c.id}'")
        val highlight = highlights[c]
        highlights.remove(c)
        drawingView.highlightContainer.remove(highlight!!)
    }

    private fun postHighlightChangedEvent(c: Collection<Component>, highlighted: Boolean) {
        eventBus.post(HighlightChangeEvent(
            highlighter = this,
            view = drawingView,
            components = c,
            highighted = highlighted))
    }
}