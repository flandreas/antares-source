package ch.scorpion.jabbah.graph.library

import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.ClassRule
import org.junit.Test

/** Unit tests for [LibraryFolder]. */
class LibraryFolderTest {

	companion object {
		@ClassRule
		@JvmField
		val rule = GraphLibraryTestRule()
	}

	@Test
	fun shouldContainDirectly() {
		val folder = LibraryFolder("test")
		val item = LibraryFolder("item")
		val item2 = LibraryFolder("item2")
		folder.add(item)

		assertThat(folder.contains(item), `is`(true))
		assertThat(folder.contains(item2), `is`(false))
	}

	@Test
	fun shouldContainRecursively() {
		val folder = LibraryFolder("test")
		val folder2 = LibraryFolder("folder2")
		val item = LibraryFolder("item")
		folder.add(folder2)
		folder2.add(item)

		assertThat(folder.contains(item), `is`(false))
		assertThat(folder.containsRecursively(item), `is`(true))
	}

	@Test
	fun shouldGetDirectly() {
		val folder = LibraryFolder("test")
		val item = LibraryFolder("item")
		val item2 = LibraryFolder("item2")

		folder.add(item)
		assertThat(folder.get("item") as LibraryFolder, sameInstance(item))
	}

	@Test
	fun shouldGetRecursively() {
		val folder = LibraryFolder("test")
		val folder2 = LibraryFolder("folder2")
		val item = LibraryFolder("item")
		folder.add(folder2)
		folder2.add(item)

		assertThat(folder.getRecursively("item") as LibraryFolder?, sameInstance(item))
		assertThat(folder.getRecursively("bla") as LibraryFolder?, `is`(nullValue()));
	}

	@Test
	fun shouldMoveItemUp() {
		val folder = LibraryFolder("test")
		val item1 = LibraryFolder("item1")
		val item2 = LibraryFolder("item2")
		val item3 = LibraryFolder("item3")
		folder.add(item1)
		folder.add(item2)
		folder.add(item3)

		folder.move(item1, 2)

		assertThat(folder.indexOf(folder.get("item2")!!), `is`(0))
		assertThat(folder.indexOf(folder.get("item3")!!), `is`(1))
		assertThat(folder.indexOf(folder.get("item1")!!), `is`(2))
	}

	@Test
	fun shouldMoveItemOneUp() {
		val folder = LibraryFolder("test")
		val item1 = LibraryFolder("item1")
		val item2 = LibraryFolder("item2")
		val item3 = LibraryFolder("item3")
		folder.add(item1)
		folder.add(item2)
		folder.add(item3)

		folder.move(item1, 1)

		assertThat(folder.indexOf(folder.get("item2")!!), `is`(0))
		assertThat(folder.indexOf(folder.get("item1")!!), `is`(1))
		assertThat(folder.indexOf(folder.get("item3")!!), `is`(2))
	}

	@Test
	fun shouldMoveItemDown() {
		val folder = LibraryFolder("test")
		val item1 = LibraryFolder("item1")
		val item2 = LibraryFolder("item2")
		val item3 = LibraryFolder("item3")
		folder.add(item1)
		folder.add(item2)
		folder.add(item3)

		folder.move(item3, 0)

		assertThat(folder.indexOf(folder.get("item3")!!), `is`(0))
		assertThat(folder.indexOf(folder.get("item1")!!), `is`(1))
		assertThat(folder.indexOf(folder.get("item2")!!), `is`(2))
	}

	@Test
	fun shouldMoveItemOneDown() {
		val folder = LibraryFolder("test")
		val item1 = LibraryFolder("item1")
		val item2 = LibraryFolder("item2")
		val item3 = LibraryFolder("item3")
		folder.add(item1)
		folder.add(item2)
		folder.add(item3)

		folder.move(item3, 1)

		assertThat(folder.indexOf(folder.get("item1")!!), `is`(0))
		assertThat(folder.indexOf(folder.get("item3")!!), `is`(1))
		assertThat(folder.indexOf(folder.get("item2")!!), `is`(2))
	}

	@Test
	fun shouldMoveItemToSamePosition() {
		val folder = LibraryFolder("test")
		val item1 = LibraryFolder("item1")
		val item2 = LibraryFolder("item2")
		val item3 = LibraryFolder("item3")
		folder.add(item1)
		folder.add(item2)
		folder.add(item3)

		folder.move(item2, 1)

		assertThat(folder.indexOf(folder.get("item1")!!), `is`(0))
		assertThat(folder.indexOf(folder.get("item2")!!), `is`(1))
		assertThat(folder.indexOf(folder.get("item3")!!), `is`(2))
	}

}