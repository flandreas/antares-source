package ch.scorpion.jabbah.edit.highlight

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.edit.*

/**
 * Module definitions for the [ch.scorpion.jabbah.edit.highlight] module.
 */
object EditHighlightModule : AbstractModule() {

    var highlighterFactory = object : HighlighterFactory {
        override fun create(content: DrawingViewContent<*>): Highlighter {
            return BelowSmHighlighter(content = content)
        }
    }

    override fun initialize() {
        // empty
    }
}