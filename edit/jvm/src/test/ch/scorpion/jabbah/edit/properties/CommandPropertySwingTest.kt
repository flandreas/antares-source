package ch.scorpion.jabbah.edit.properties

import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.edit.BeanProvider
import ch.scorpion.jabbah.edit.EditTestRule
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.command.AppendCommand
import ch.scorpion.jabbah.edit.command.ApplicationDummy
import ch.scorpion.jabbah.edit.command.SourcingCommandManager
import ch.scorpion.jabbah.edit.command.StorableString
import ch.scorpion.jabbah.io.IOModule
import io.mockk.every
import io.mockk.mockk
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class CommandPropertySwingTest {

	companion object {
		init {
			Translations.withAnyKey()
			EditTestRule.configure()
			IOModule.typeMap.register("storableString", StorableString::class)
		}
	}

	private var app = ApplicationDummy()

	private var cmdManager = SourcingCommandManager()

	private val editor = createEditor()

	private val beanProvider: BeanProvider = { _,_ -> app }

	private val property = CommandPropertySwing(
		"data",
		"baseKey",
		StorableString::class.java,
		beanProvider)

	init {
		cmdManager.bindDataHolder(app)
		property.bind(editor, 0)
	}

	@Test
	fun shouldWritePropertyToBean() {
		property.value = StorableString("fromProperty")
		property.writeToBean()

		assertEquals(app.data!!.value, "fromProperty")
	}

	@Test
	fun shouldRollbackOnException() {
		cmdManager.execute(AppendCommand(app, "a"))

		try {
			property.value = StorableString("throwException")
			property.writeToBean()
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

	private fun createEditor(): Editor {
		val editor =  mockk<Editor>(relaxed = true)
		every { editor.commandManager } returns cmdManager
		return editor
	}
}