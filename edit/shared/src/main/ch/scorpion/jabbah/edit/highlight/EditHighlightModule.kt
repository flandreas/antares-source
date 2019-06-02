package ch.scorpion.jabbah.edit.highlight

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.select.SelectionModelFactory
import ch.scorpion.jabbah.edit.select.SelectionModelFactoryImpl
import ch.scorpion.jabbah.edit.select.SimpleSelectionModelProvider

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.highlight] module.
 */
object EditHighlightModule : AbstractModule() {

	var highlightModelFactory: SelectionModelFactory = SelectionModelFactoryImpl()

	var highlightModelProvider: SelectionModelProvider = SimpleSelectionModelProvider(highlightModelFactory)

    var highlighterFactory = object : HighlighterFactory {
        override fun create(content: DrawingViewContent<*>): Highlighter {
            return BelowSmHighlighter(content = content)
        }
    }

    override fun initialize() {
        // empty
    }
}