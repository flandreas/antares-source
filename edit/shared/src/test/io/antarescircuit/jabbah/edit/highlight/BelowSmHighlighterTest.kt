package io.antarescircuit.jabbah.edit.highlight

import io.antarescircuit.jabbah.draw.graphics.Color
import io.antarescircuit.jabbah.draw.graphics.CompositeColor
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.select.BoundingBoxBelowSelectionModel
import io.antarescircuit.jabbah.edit.view.DrawingViewContentImpl
import io.antarescircuit.jabbah.edit.view.DrawingViewImpl
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BelowSmHighlighterTest {

	private val selectionModelProvider = mock<SelectionModelProvider>(MockMode.autofill)

	private val highlighterFactory = object : HighlighterFactory {
		override fun create(content: DrawingViewContent<*, *>): Highlighter {
			return BelowSmHighlighter(highlightModelProvider = selectionModelProvider, content = content)
		}
	}

	private val drawingView: DrawingViewImpl<Component, Drawing<Component>>

	private val content: DrawingViewContentImpl<Component, Drawing<Component>>

	private val highlightColor: CompositeColor

	private val rect: RectangleComponent

	private val highlight: BoundingBoxBelowSelectionModel

	init {
		EditTestRule.configure()
		drawingView = DrawingViewImpl(mock(MockMode.autofill))
		content = DrawingViewContentImpl(
			drawingView = drawingView,
			drawing = drawingView.drawing,
			selectionManagerFactory = { mock(MockMode.autofill) },
			highlighterFactory = highlighterFactory)
		highlightColor = CompositeColor(backgroundColor = Color.YELLOW)
		rect = RectangleComponent()
		highlight = BoundingBoxBelowSelectionModel(rect)

		every { selectionModelProvider.provideFor(any(), any()) } calls { highlight }
	}

	@Test
	fun shouldHighlight() {
		content.highlighter.highlight(rect, highlightColor)

		assertTrue(content.highlighter.isHighlighted(rect))
		assertEquals(1, content.highlightContainer.drawables.size)
		assertTrue(content.highlightContainer.contains(highlight))
	}

	@Test
	fun shouldUnhighlight() {
		content.highlighter.highlight(rect, highlightColor)
		content.highlighter.unhighlight(rect)

		assertFalse(content.highlighter.isHighlighted(rect))
		assertEquals(0, content.highlightContainer.drawables.size)
	}

	@Test
	fun shouldReturnHighlight() {
		content.highlighter.highlight(rect, highlightColor)

		assertEquals(highlight, content.highlighter.getHighlightFor(rect))
	}

	@Test
	fun shouldApplyColor() {
		content.highlighter.highlight(rect, highlightColor)

		assertEquals(Color.YELLOW, highlight.color.backgroundColor)
	}

	@Test
	fun shouldReplaceColor() {
		content.highlighter.highlight(rect, highlightColor)

		content.highlighter.replaceColor(highlightColor, CompositeColor(backgroundColor = Color.BLUE))

		assertEquals(Color.BLUE, highlight.color.backgroundColor)
	}
}