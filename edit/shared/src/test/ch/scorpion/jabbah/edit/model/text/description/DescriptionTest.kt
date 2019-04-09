package ch.scorpion.jabbah.edit.model.text.description

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import io.mockk.mockk
import io.mockk.verify
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.BeforeTest

class DescriptionTest {

	@BeforeTest
	fun setup() {
		EditModuleJvm.require()
	}

	@Test
	fun shouldPostEventUponChange() {
		val eventBus = mockk<EventBus>(relaxed = true)
		val description = Description(TranslatableText("tree"), eventBus)

		description.translation = TranslatableText("car")

		assertEquals("car", description.value)
		verify { eventBus.post(any()) }
	}
}