package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.EditTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import javax.swing.JTextField

private class TestPoint2DEditor(errorCallback: (IllegalArgumentException) -> Unit) : Point2DEditor(errorCallback) {
	fun setText(text: String) {
		(editor as JTextField).text = text
	}
}

class Point2DPropertyTest {

	init {
		EditTestRule.configure()
	}

	@Test
	fun rendererShouldFormatCoordinates() {
		assertEquals("100,200", Point2DRenderer.format(Point2D(100, 200)))
		assertEquals("1.5,-2.25", Point2DRenderer.format(Point2D(1.5, -2.25)))
	}

	@Test
	fun editorShouldReadCoordinatesFromTextField() {
		val editor = Point2DEditor({})
		editor.setValue(Point2D(100, 200))

		assertEquals(Point2D(100, 200), editor.value)
		assertEquals(Point2D(1.5, -2.25), Point2DEditor.parse(" 1.5, -2.25 "))
	}

	@Test
	fun editorShouldRejectInvalidCoordinates() {
		assertFailsWith<IllegalArgumentException> { Point2DEditor.parse("100") }
		val exception = assertFailsWith<IllegalArgumentException> { Point2DEditor.parse("-50,-AB") }
		assertEquals("Enter the location as x,y using numeric coordinates.", exception.message)
		assertFailsWith<IllegalArgumentException> { Point2DEditor.parse("NaN,200") }
	}

	@Test
	fun editorShouldReturnValidationSentinelWhenEditingStopsWithInvalidText() {
		var error: IllegalArgumentException? = null
		val editor = TestPoint2DEditor { error = it }
		editor.setValue(Point2D(-50, -100))
		editor.setText("-50,-AB")

		assertEquals(Unit, editor.value)
		assertEquals("Enter the location as x,y using numeric coordinates.", error?.message)
	}
}
