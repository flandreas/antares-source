package io.antarescircuit.jabbah.edit.module

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.edit.model.text.EditModelTextModule
import io.antarescircuit.jabbah.edit.model.text.SimpleTextComponent
import io.antarescircuit.jabbah.edit.model.text.TextComponent
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.io.IOModule
import io.antarescircuit.jabbah.io.StorableCloner
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class EditModuleJvmTest {

	@Test
	fun shouldCreateAndPersistTextWithoutDesktopUi() {
		EditModuleJvm.require()
		val text = EditModelTextModule.textComponentFactory.create(
			TranslatableText("Core text"),
			Point2D(20, 30),
		)
		assertIs<SimpleTextComponent>(text)
		assertIs<SimpleTextComponent>(IOModule.typeMap.instantiate<TextComponent>("text"))

		val copy = StorableCloner.clone(text)
		assertEquals(text.text, copy.text)
		assertEquals(text.location, copy.location)
	}
}
