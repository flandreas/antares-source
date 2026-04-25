package io.antarescircuit.jabbah.edit.highlight

import io.antarescircuit.jabbah.base.AbstractModule
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.select.BoundingBoxBelowSelectionModel
import io.antarescircuit.jabbah.edit.select.SelectionModelFactory
import io.antarescircuit.jabbah.edit.select.SelectionModelFactoryImpl
import io.antarescircuit.jabbah.edit.select.SimpleSelectionModelProvider
import io.antarescircuit.jabbah.edit.style.EditStyleType

/**
 * Module definitions for the [io.antarescircuit.jabbah.edit.highlight] module.
 */
object EditHighlightModule : AbstractModule() {

	var highlightModelFactory: SelectionModelFactory = SelectionModelFactoryImpl(
        mapOf(SelectionDrawingStrategy.BELOW to {component -> BoundingBoxBelowSelectionModel(component, styleType = EditStyleType.HIGHLIGHT) }))

	var highlightModelProvider: SelectionModelProvider = SimpleSelectionModelProvider(highlightModelFactory)

    var highlighterFactory = object : HighlighterFactory {
        override fun create(content: DrawingViewContent<*,*>): Highlighter {
            return BelowSmHighlighter(content = content)
        }
    }

    override fun initialize() {}

    override fun resetDependencies() {}
}