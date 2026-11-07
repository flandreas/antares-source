package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Rectangle2D
import io.antarescircuit.jabbah.base.geom.RectangularShape
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.EditTestRule
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.model.AbstractComponent
import io.antarescircuit.jabbah.edit.model.DrawingImpl
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.model.text.VerticalAlignment
import io.antarescircuit.jabbah.edit.model.text.description.BASE_KEY_NAME
import dev.mokkery.answering.returns
import dev.mokkery.mock
import org.junit.Assert.assertEquals
import org.junit.Test
import dev.mokkery.every

class PropertyCommandTest {

	private val drawing = DrawingImpl<Component>()
	private val editor = mock<Editor>()
	private val rectangle: RectangleComponent

	init {
		EditTestRule.configure()
		rectangle = RectangleComponent()
		every { editor.drawing } returns drawing
		drawing.add(rectangle)
	}

	@Test
	fun shouldExecute() {
		rectangle.verticalAlignment = VerticalAlignment.CENTER
		val cmd = createCommand("verticalAlignment", "edit.property.verticalAlignment", rectangle.id, VerticalAlignment.TOP)

		cmd.execute()

		assertEquals(VerticalAlignment.TOP, rectangle.verticalAlignment)
	}

	@Test
	fun shouldUndo() {
		rectangle.verticalAlignment = VerticalAlignment.CENTER
		val cmd = createCommand("verticalAlignment", "edit.property.verticalAlignment", rectangle.id, VerticalAlignment.TOP)

		cmd.execute()
		cmd.undo()

		assertEquals(VerticalAlignment.CENTER, rectangle.verticalAlignment)
	}

	@Test
	fun shouldExecuteNestedProperty() {
		val component = ComponentWithNestedProperty(NestedProperty("oldName"))
		drawing.add(component)
		val cmd = createCommand("property.value", BASE_KEY_NAME, component.id, "newName")

		cmd.execute()

		assertEquals("newName", component.property.value)
	}
	@Test
	fun shouldUndoNestedProperty() {
		val component = ComponentWithNestedProperty(NestedProperty("oldName"))
		drawing.add(component)
		val cmd = createCommand("property.value", BASE_KEY_NAME, component.id,"newName")

		cmd.execute()
		cmd.undo()

		assertEquals("oldName", component.property.value)
	}

	private fun createCommand(name: String, baseKey: String, id: Int, newValue: Any): PropertyCommandSwing<Any> {
		return PropertyCommandSwing.forComponent(
			editor = editor,
			propertyBaseKey = baseKey,
			beanIds = listOf(id.toString()),
			newValue = newValue,
			setterPropertyName = name,
			getterPropertyName = name
		)
	}

	class NestedProperty(value: String) {
		var value: String = value
	}

	class ComponentWithNestedProperty(
		var property: NestedProperty
	) : AbstractComponent() {
		override var location: Point2D = Point2D()
		override val type: String = "type"
		override val boundingBox: RectangularShape = Rectangle2D()
		override fun draw(context: DrawContext) { }
		override fun contains(x: Double, y: Double): Boolean = false
	}
}