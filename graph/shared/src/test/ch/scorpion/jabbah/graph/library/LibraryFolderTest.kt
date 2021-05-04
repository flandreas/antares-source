package ch.scorpion.jabbah.graph.library

import kotlin.test.*

/** Unit tests for [LibraryFolder]. */
class LibraryFolderTest {

	companion object {
		init {
			GraphLibraryTestRule.configure()
		}
	}

	@Test
	fun shouldContainDirectly() {
		val folder = LibraryFolder("test")
		val item = LibraryFolder("item")
		val item2 = LibraryFolder("item2")
		folder.add(item)

		assertTrue(folder.contains(item))
		assertFalse(folder.contains(item2))
	}

	@Test
	fun shouldContainRecursively() {
		val folder = LibraryFolder("test")
		val folder2 = LibraryFolder("folder2")
		val item = LibraryFolder("item")
		folder.add(folder2)
		folder2.add(item)

		assertFalse(folder.contains(item))
		assertTrue(folder.containsRecursively(item))
	}

	@Test
	fun shouldGetDirectly() {
		val folder = LibraryFolder("test")
		val item = LibraryFolder("item")

		folder.add(item)
		assertSame(item, folder.get("item") as LibraryFolder)
	}

	@Test
	fun shouldGetRecursively() {
		val folder = LibraryFolder("test")
		val folder2 = LibraryFolder("folder2")
		val item = LibraryFolder("item")
		folder.add(folder2)
		folder2.add(item)

		assertSame(item, folder.getRecursively("item") as LibraryFolder?)
		assertNull(folder.getRecursively("bla") as LibraryFolder?)
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

		assertEquals(0, folder.indexOf(folder.get("item2")!!))
		assertEquals(1, folder.indexOf(folder.get("item3")!!))
		assertEquals(2, folder.indexOf(folder.get("item1")!!))
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

		assertEquals(0, folder.indexOf(folder.get("item2")!!))
		assertEquals(1, folder.indexOf(folder.get("item1")!!))
		assertEquals(2, folder.indexOf(folder.get("item3")!!))
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

		assertEquals(0, folder.indexOf(folder.get("item3")!!))
		assertEquals(1, folder.indexOf(folder.get("item1")!!))
		assertEquals(2, folder.indexOf(folder.get("item2")!!))
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

		assertEquals(0, folder.indexOf(folder.get("item1")!!))
		assertEquals(1, folder.indexOf(folder.get("item3")!!))
		assertEquals(2, folder.indexOf(folder.get("item2")!!))
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

		assertEquals(0, folder.indexOf(folder.get("item1")!!))
		assertEquals(1, folder.indexOf(folder.get("item2")!!))
		assertEquals(2, folder.indexOf(folder.get("item3")!!))
	}
}