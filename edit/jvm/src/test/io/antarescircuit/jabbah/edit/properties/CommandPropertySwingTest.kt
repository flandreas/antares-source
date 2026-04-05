package io.antarescircuit.jabbah.edit.properties

import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.edit.*
import io.antarescircuit.jabbah.edit.command.AppendCommand
import io.antarescircuit.jabbah.edit.command.ApplicationDummy
import io.antarescircuit.jabbah.edit.command.SourcingCommandManager
import io.antarescircuit.jabbah.edit.command.StorableString
import io.antarescircuit.jabbah.io.IOModule
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.mock
import dev.mokkery.every
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommandPropertySwingTest {

	private var app = ApplicationDummy()

	private var cmdManager = SourcingCommandManager()

	private val editor = createEditor()

	private val beanProvider: BeanProvider = { _,_ -> listOf(app) }

	private val property = CommandPropertySwing(
		"data",
		"baseKey",
		StorableString::class.java,
		beanProvider)

	init {
		Translations.withAnyKey()
		EditTestRule.configure()
		IOModule.typeMap.register("storableString", StorableString::class)

		cmdManager.bindDataHolder(app)
		property.bind(editor, listOf("0"))
	}

	@Test
	fun shouldWritePropertyToBean() {
		property.value = StorableString("fromProperty")
		property.writeToBeans()

		assertEquals(app.data!!.value, "fromProperty")
	}

	@Test
	fun shouldRollbackOnException() {
		cmdManager.execute(AppendCommand(app, "a"))

		try {
			property.value = StorableString("throwException")
			property.writeToBeans()
		} catch (e: Throwable) {
			// This emulates the PropertyPanel catching and displaying the exception
		}

		cmdManager.execute(AppendCommand(app, "b"))
		assertEquals("ab", app.mandatoryData.value)

		assertTrue(cmdManager.canUndo())
		cmdManager.undo()
		assertEquals("a", app.mandatoryData.value)

		cmdManager.undo()
		assertEquals("", app.mandatoryData.value)
	}

	@Test
	fun shouldRollbackOnExceptionWithEmptyUndoStack() {
		try {
			property.value = StorableString("throwException")
			property.writeToBeans()
		} catch (_: Throwable) {
			// This emulates the PropertyPanel catching and displaying the exception
		}

		cmdManager.execute(AppendCommand(app, "a"))
		assertTrue(cmdManager.canUndo())
		cmdManager.undo()
		assertEquals("", app.mandatoryData.value)
	}

	private fun createEditor(): Editor {
		val editor =  mock<Editor>(MockMode.autofill)
		val view = DrawingViewMockBuilder().build<Component>()
		every { editor.commandManager } returns cmdManager
		every { editor.view } returns view
		return editor
	}
}