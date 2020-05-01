package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.geom.Rectangle2D
import ch.scorpion.jabbah.base.geom.RectangularShape
import ch.scorpion.jabbah.draw.DrawContext
import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.AbstractComponent
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import ch.scorpion.jabbah.edit.model.text.description.Namable
import ch.scorpion.jabbah.edit.model.text.description.NamableImpl
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
		rectangle.alignment = VerticalAlignment.CENTER
		val cmd = createCommand("alignment", "edit.property.verticalAlignment", rectangle.id, VerticalAlignment.TOP)

		cmd.execute()

		assertEquals(VerticalAlignment.TOP, rectangle.alignment)
	}

	@Test
	fun shouldUndo() {
		rectangle.alignment = VerticalAlignment.CENTER
		val cmd = createCommand("alignment", "edit.property.verticalAlignment", rectangle.id, VerticalAlignment.TOP)

		cmd.execute()
		cmd.undo()

		assertEquals(VerticalAlignment.CENTER, rectangle.alignment)
	}

	@Test
	fun shouldExecuteNestedProperty() {
		val namable = NamedComponent("oldName")
		drawing.add(namable)
		val cmd = createCommand("name.translation", "edit.property.name", namable.id, TranslatableText("newName"))

		cmd.execute()

		assertEquals("newName", namable.name.value)
	}
	@Test
	fun shouldUndoNestedProperty() {
		val namable = NamedComponent("oldName")
		drawing.add(namable)
		val cmd = createCommand("name.translation", "edit.property.name", namable.id, TranslatableText("newName"))

		cmd.execute()
		cmd.undo()

		assertEquals("oldName", namable.name.value)
	}

	private fun createCommand(name: String, baseKey: String, id: Int, newValue: Any): Command {
		return PropertyCommand.forComponent(
			editor = editor,
			propertyBaseKey = baseKey,
			beanIds = listOf(id),
			newValue = newValue,
			setterPropertyName = name,
			getterPropertyName = name
		)
	}

	private class NamedComponent(
		name: String = "initialName",
		private val namable: Namable = NamableImpl(name)
	) : AbstractComponent(), Namable by namable {
		override var location: Point2D = Point2D()
		override val type: String = "type"
		override val boundingBox: RectangularShape = Rectangle2D()

		override fun draw(context: DrawContext) { }

		override fun contains(x: Double, y: Double): Boolean = false

	}
}