package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.edit.Command
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.model.DrawingImpl
import ch.scorpion.jabbah.edit.model.rectangle.RectangleComponent
import ch.scorpion.jabbah.edit.model.text.VerticalAlignment
import io.mockk.every
import io.mockk.mockk
import org.junit.Assert.*
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
		val cmd = createCommand(VerticalAlignment.TOP)

		cmd.execute()

		assertEquals(VerticalAlignment.TOP, rectangle.alignment)
	}

	@Test
	fun shouldUndo() {
		rectangle.alignment = VerticalAlignment.CENTER
		val cmd = createCommand(VerticalAlignment.TOP)

		cmd.execute()
		cmd.undo()

		assertEquals(VerticalAlignment.CENTER, rectangle.alignment)
	}

	private fun createCommand(newValue: VerticalAlignment): Command {
		return PropertyCommand.forComponent(
			editor,
			"edit.property.verticalAlignment",
			rectangle.id,
			newValue,
			"alignment",
			"alignment"
		)
	}
}