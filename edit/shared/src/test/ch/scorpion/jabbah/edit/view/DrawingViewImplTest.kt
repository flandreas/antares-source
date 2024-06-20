package ch.scorpion.jabbah.edit.view

import ch.scorpion.jabbah.base.geom.AffineTransformImpl
import ch.scorpion.jabbah.base.geom.Dimension2D
import ch.scorpion.jabbah.draw.Canvas
import ch.scorpion.jabbah.draw.view.SimpleViewPainter
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.highlight.EditHighlightModule
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.select.EditSelectModule
import dev.mokkery.*
import dev.mokkery.answering.returns
import dev.mokkery.verify.VerifyMode.Companion.exactly
import kotlin.test.Test

/** Unit tests for [DrawingViewImpl].*/
class DrawingViewImplTest {

	init {
		EditTestRule.configure()
	}

	private val drawing = DrawingImpl<Component>()

	private val canvas: Canvas = createCanvas()

	private val view = DrawingViewImpl<Drawing<Component>>(
		drawing = drawing,
		transformFactory = { AffineTransformImpl() },
		viewPainterFactory = { SimpleViewPainter(it) }
	)

	init {
		view.canvas = canvas
	}

	@Test
	fun shouldRepaintCanvasWhenValidatingDrawing() {
		drawing.validate()

		verify { canvas.repaint() }
	}

	@Test
	fun shouldNotRepaintValidatedReplacedContent() {
		val oldContent = view.content
		val newDrawing = DrawingImpl<Component>()
		val newContent = DrawingViewContentImpl(view, newDrawing, EditSelectModule.selectionManagerFactory, EditHighlightModule.highlighterFactory)
		view.content = newContent

		resetCalls(canvas)
		oldContent.drawing.validate()

		verify(exactly(0)) { canvas.repaint() }
	}

	@Test
	fun shouldRepaintValidatedReplacingContent() {
		val newDrawing = DrawingImpl<Component>()
		val newContent = DrawingViewContentImpl(view, newDrawing, EditSelectModule.selectionManagerFactory, EditHighlightModule.highlighterFactory)
		view.content = newContent

		resetCalls(canvas)
		newContent.drawing.validate()

		verify { canvas.repaint() }
	}

	@Test
	fun shouldRepaintValidatedReusedContent() {
		val oldContent = view.content
		val newDrawing = DrawingImpl<Component>()
		val newContent = DrawingViewContentImpl(view, newDrawing, EditSelectModule.selectionManagerFactory, EditHighlightModule.highlighterFactory)
		view.content = newContent
		view.content = oldContent

		resetCalls(canvas)
		oldContent.drawing.validate()

		verify { canvas.repaint() }
	}

	private fun createCanvas(): Canvas {
		val canvas: Canvas = mock(MockMode.autofill)
		every { canvas.view } returns view
		every { canvas.dimension } returns Dimension2D(100, 100)
		return canvas
	}
}