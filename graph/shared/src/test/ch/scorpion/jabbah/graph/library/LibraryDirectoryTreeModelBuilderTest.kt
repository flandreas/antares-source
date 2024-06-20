package ch.scorpion.jabbah.graph.library

import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.edit.model.text.description.Name
import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.mock
import kotlin.test.Test
import kotlin.test.assertEquals

class LibraryDirectoryTreeModelBuilderTest {

	companion object {
		init {
			GraphLibraryTestRule.configure()
		}
	}

	private fun sampleDirectory(): LibraryDirectory {
		return LibraryFolder("1")
			.add(item("1.1"))
			.add(LibraryFolder("1.2")
				.add(item("1.2.1"))
				.add(item("1.2.2")))
			.add(item("1.3"))
	}

	@Test
	fun shouldBuildTree() {
		val directory = sampleDirectory()

		val model = LibraryDirectoryTreeModelBuilder(directory).build()

		assertEquals("1", model.item.name.value)
		assertEquals("1.1", model.children[0].item.name.value)
		assertEquals("1.2", model.children[1].item.name.value)
		assertEquals("1.2.1", model.children[1].children[0].item.name.value)
		assertEquals("1.2.2", model.children[1].children[1].item.name.value)
		assertEquals("1.3", model.children[2].item.name.value)
	}

	@Test
	fun shouldFilter() {
		val directory = sampleDirectory()

		val model = LibraryDirectoryTreeModelBuilder(directory) { it.name.value == "1.2.1" }.build()

		assertEquals("1", model.item.name.value)
		assertEquals("1.2", model.children[0].item.name.value)
		assertEquals("1.2.1", model.children[0].children[0].item.name.value)
	}

	private fun item(name: String): LibraryItem {
		val item = mock<LibraryItem>(MockMode.autofill)
		every { item.name } returns Name(TranslatableText(name))
		return item
	}
}