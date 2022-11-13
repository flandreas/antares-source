package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.edit.model.text.description.BASE_KEY_NAME
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Test

class PropertyCommandTest {

	companion object {
		init {
			EditTestRule.configure()
		}

	}

	private val drawing = DrawingImpl<Component>()
	private val editor = mockk<Editor>()
	private val rectangle = RectangleComponent()

	init {
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