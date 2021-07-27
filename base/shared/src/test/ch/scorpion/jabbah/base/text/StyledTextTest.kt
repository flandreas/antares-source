package ch.scorpion.jabbah.base.text

import kotlin.test.*

class StyledTextTest {

	@Test
	fun shouldCombineEqualChunks() {
		val styledText = StyledTextBuilder()
			.append("Hello, ")
			.append("Jabbah!")
			.build()

		assertEquals(14, styledText.length)
		assertEquals(1, styledText.chunkCount)
	}

	@Test
	fun shouldBuildStyledText() {
		val styledText = StyledTextBuilder()
			.append("Hello, ")
			.appendBold("Jabbah!")
			.build()

		assertEquals(14, styledText.length)
		assertEquals(2, styledText.chunkCount)
		assertFalse(styledText.isBold(0))
		assertTrue(styledText.isBold(7))
	}

	@Test
	fun shouldBuildStyleRange() {
		val styledText = StyledTextBuilder()
			.append("Hello, ")
			.beginBold()
			.append("Jabbah.")
			.append(" Still Bold!")
			.endBold()
			.append("Back to normal.")
			.build()

		assertEquals(3, styledText.chunkCount)
		assertFalse(styledText.isBold(0))
		assertTrue(styledText.isBold(7))
		assertTrue(styledText.isBold(13))
		assertFalse(styledText.isBold(30))
	}

	@Test
	fun shouldSplit() {
		val styledText = StyledTextBuilder()
			.append("Hello, Jabbah!")
			.build()

		val split = styledText.split(' ')

		assertEquals(2, split.size)
		assertEquals(1, split[0].chunkCount)
		assertEquals(1, split[1].chunkCount)
	}

	@Test
	fun shouldSplitWithStyles() {
		val styledText = StyledTextBuilder()
			.append("Hello, ")
			.appendBold("Jabbah!")
			.build()

		val split = styledText.split(' ')

		assertEquals(2, split.size)
		assertEquals(1, split[0].chunkCount)
		assertEquals(1, split[1].chunkCount)
	}

	@Test
	fun shouldNotSplitSingleLineWithStyle() {
		val styledText = StyledTextBuilder()
			.append("Hello, ")
			.appendBold("Jabbah!")
			.build()

		val split = styledText.splitLines()

		assertEquals(1, split.size)
		assertEquals(2, split[0].chunkCount)
	}

	@Test
	fun shouldAppendStyledText() {
		val styledText = StyledTextBuilder()
			.append("Hello, ")
			.append(StyledTextBuilder().appendBold("Jabbah!").build())
			.build()

		assertEquals(2, styledText.chunkCount)
		assertFalse(styledText.isBold(0))
		assertTrue(styledText.isBold(7))
	}
}