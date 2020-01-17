package ch.scorpion.antares.model.memory

import ch.scorpion.antares.AntaresTestRule
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class MemoryDumpFileVersionTest {

	companion object {
		init {
			AntaresTestRule.configure()
		}
	}

	@Test
	fun shouldExtractFromVersionId() {
		val version = MemoryDumpFileVersion.extractFrom("#amd-df-0.1 some content")
		assertEquals(MemoryDumpVersionType.Default, version!!.type)
		assertEquals("0.1", version.number)
	}

	@Test
	fun shouldRejectTooShortVersionId() {
		assertNull(MemoryDumpFileVersion.extractFrom("amd-df"))
	}
}