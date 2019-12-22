package ch.scorpion.jabbah.edit.highlight

import ch.scorpion.jabbah.draw.graphics.Color
import ch.scorpion.jabbah.draw.graphics.CompositeColor
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.select.BoundingBoxBelowSelectionModel
import ch.scorpion.jabbah.edit.view.DrawingViewContentImpl
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class BelowSmHighlighterTest {

	companion object {
		init {
			EditTestRule.configure()
		}
	}

	private val selectionModelProvider = mockk<SelectionModelProvider>(relaxed = true)

	private val highlighterFactory = object : HighlighterFactory {
		override fun create(content: DrawingViewContent<*>): Highlighter {
			return BelowSmHighlighter(highlightModelProvider = selectionModelProvider, content = content)
		}
	}

	private val content = DrawingViewContentImpl<Drawing<Component>>(
		drawingView = mockk(relaxed = true),
		drawing = mockk(relaxed = true),
		selectionManagerFactory = mockk(relaxed = true),
		highlighterFactory = highlighterFactory)

	private val highlightColor = CompositeColor(backgroundColor = Color.YELLOW)

	private val rect = RectangleComponent()

	private val hightlight = BoundingBoxBelowSelectionModel(rect)

	init {
		/*
		val slot = slot<AbstractRectangularComponent>()
		every { selectionModelProvider.provideFor(component = capture(slot), strategy = SelectionDrawingStrategy.BELOW )} answers { RectangularBelowSelectionModel(slot.captured)}
		*/
		every { selectionModelProvider.provideFor(any(), any()) } answers { hightlight }
	}

	@Test
	fun shouldHighlight() {
		content.highlighter.highlight(rect, highlightColor)

		assertTrue(content.highlighter.isHighlighted(rect))
		assertEquals(1, content.highlightContainer.drawablesCount)
		assertTrue(content.highlightContainer.contains(hightlight))
	}

	@Test
	fun shouldUnhighlight() {
		content.highlighter.highlight(rect, highlightColor)
		content.highlighter.unhighlight(rect)

		assertFalse(content.highlighter.isHighlighted(rect))
		assertEquals(0, content.highlightContainer.drawablesCount)
	}

	@Test
	fun shouldReturnHighlight() {
		content.highlighter.highlight(rect, highlightColor)

		assertEquals(hightlight, content.highlighter.getHighlightFor(rect))
	}

	@Test
	fun shouldApplyColor() {
		content.highlighter.highlight(rect, highlightColor)

		assertEquals(Color.YELLOW, hightlight.color.backgroundColor)
	}

	@Test
	fun shouldReplaceColor() {
		content.highlighter.highlight(rect, highlightColor)

		content.highlighter.replaceColor(highlightColor, CompositeColor(backgroundColor = Color.BLUE))

		assertEquals(Color.BLUE, hightlight.color.backgroundColor)
	}
}