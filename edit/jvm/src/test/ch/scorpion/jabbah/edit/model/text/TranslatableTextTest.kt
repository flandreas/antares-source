package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Language.English
import ch.scorpion.jabbah.base.Language.German
import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.edit.module.EditModule
import ch.scorpion.jabbah.io.*
import kotlin.test.*

class TranslatableTextTest {

	@BeforeTest
	fun setup() {
		EditModule.require()
	}

	@Test
	fun shouldSetAndGetTranslation() {
		val text = TranslatableText()
			.withTranslation(German, "Baum")
			.withTranslation(English, "Tree")

		assertEquals("Baum", text.getTranslation(German))
		assertEquals("Tree", text.getTranslation(English))
	}

	@Test
	fun shouldRemoveTranslation() {
		var text = TranslatableText()
			.withTranslation(German, "Baum")
			.withTranslation(English, "Tree")

		text = text.withoutTranslation(English)

		assertFalse(text.hasTranslation(English))
	}

	@Test
	fun shouldRemoveTranslationForSystemLanguage() {
		java.lang.System.setProperty("user.language", "de")
		var text: Translatable = TranslatableText()
			.withTranslation(German, "Baum")
			.withTranslation(English, "Tree")

		text = text.withoutTranslation()

		assertFalse(text.hasTranslation(German))
	}

	@Test
	fun shouldHaveTranslation() {
		val text = TranslatableText(German, "Baum")

		assertTrue(text.hasTranslation(German))
		assertFalse(text.hasTranslation(English))
	}

	@Test(expected = IllegalArgumentException::class)
	fun shouldNotAcceptEmptyTranslation() {
		val text = TranslatableText()
		text.withTranslation(German, "")
	}

	@Test
	fun shouldYieldTranslationInSystemLanguage() {
		val text = TranslatableText()
			.withTranslation(German, "Baum")
			.withTranslation(English, "Tree")

		assertEquals(text.getTranslation(System.currentLanguage()), text.getTranslation())
	}

	@Test
	fun shouldFallbackToDefaultLanguage() {
		java.lang.System.setProperty("user.language", "de")
		val text = TranslatableText(English, "Tree")

		assertEquals("Tree", text.getTranslation())
	}

	@Test
	fun shouldFallbackToAnyLanguageWithMissingDefault() {
		java.lang.System.setProperty("user.language", English.code)
		val text = TranslatableText(German, "Baum")

		assertEquals("Baum", text.getTranslation())
	}

	@Test
	fun shouldWriteAndRead() {
		val obj = ClassUsingTranslatable(TranslatableText(listOf(
			Translation(German, "Meer"),
			Translation(English, "Sea")
		)))
		IOModule.typeMap.register("foo", ClassUsingTranslatable::class)
		IOModule.typeMap.register("translation", Translation::class)

		val clone = StorableCloner.clone(obj)

		assertEquals("Meer", clone.attribute.getTranslation(German))
		assertEquals("Sea", clone.attribute.getTranslation(English))
	}

	@Test
	fun shouldBeEqualWithSameTranslations() {
		val text1 = TranslatableText("Text")
		val text2 = TranslatableText("Text")
		assertEquals(text1, text2)
	}

	@Test
	fun shouldBeDifferentWithDifferentTranslations() {
		val text1 = TranslatableText("Text")
		val text2 = TranslatableText("Text2")
		assertNotEquals(text1, text2)
	}

	@Test
	fun shouldBeImmutable() {
		val text1 = TranslatableText("Text")
		val text2 = text1.withTranslation("Text2")
		assertNotSame(text1, text2)
	}

	@Test
	fun shouldBeEmpty() {
		val text = TranslatableText()
		assertTrue(text.isEmpty)
	}

	@Test
	fun shouldBeEmptyWithEmptyText() {
		val text = TranslatableText("")
		assertTrue(text.isEmpty)
	}

	@Test
	fun shouldTranslateOptionally() {
		java.lang.System.setProperty("user.language", "en")
		assertNull(TranslatableText().getOptionalTranslation())
		assertEquals("Tree", TranslatableText(English, "Tree").getOptionalTranslation())
	}

	@Test
	fun shouldDetectAnyEqualTranslation() {
		val text = TranslatableText()
			.withTranslation(German, "Baum")
			.withTranslation(English, "Tree")

		assertTrue(text.isAnyEqualOf(TranslatableText().withTranslation(German, "Baum")))
		assertFalse(text.isAnyEqualOf(TranslatableText().withTranslation(English, "True")))
		assertFalse(text.isAnyEqualOf(TranslatableText().withTranslation(German, "Apfel")))
		assertFalse(text.isAnyEqualOf(TranslatableText()))
		assertFalse(TranslatableText().isAnyEqualOf(TranslatableText().withTranslation(German, "Baum")))
	}
}

class ClassUsingTranslatable(text: TranslatableText? = null) : AbstractStorable() {

	var attribute: TranslatableText = text ?: TranslatableText()
		private set

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun write(writer: StoreWriter) {
		writer.writeStorables("name", attribute.allTranslations())
	}

	override fun read(reader: StoreReader) {
		attribute = TranslatableText(reader.readStorables("name"))
	}
}
