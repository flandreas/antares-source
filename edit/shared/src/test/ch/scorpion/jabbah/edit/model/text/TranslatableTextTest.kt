package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.System
import ch.scorpion.jabbah.base.Language.*
import ch.scorpion.jabbah.base.exception.IllegalArgumentException
import ch.scorpion.jabbah.edit.module.EditModuleJvm
import ch.scorpion.jabbah.io.*
import org.hamcrest.CoreMatchers.*
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import java.util.*

class TranslatableTextTest {

	@Before
	fun setup() {
		EditModuleJvm.require()
	}

	@Test
	fun shouldSetAndGetTranslation() {
		val text = TranslatableText()
			.withTranslation(German, "Baum")
			.withTranslation(English, "Tree")

		assertThat(text.getTranslation(German), `is`("Baum"))
		assertThat(text.getTranslation(English), `is`("Tree"))
	}

	@Test
	fun shouldRemoveTranslation() {
		var text = TranslatableText()
			.withTranslation(German, "Baum")
			.withTranslation(English, "Tree")

		text = text.withoutTranslation(English)

		assertThat(text.hasTranslation(English), `is`(false))
	}

	@Test
	fun shouldRemoveTranslationForSystemLanguage() {
		java.lang.System.setProperty("user.language", "de")
		var text = TranslatableText()
			.withTranslation(German, "Baum")
			.withTranslation(English, "Tree")

		text = text.withoutTranslation()

		assertThat(text.hasTranslation(German), `is`(false))
	}

	@Test
	fun shouldHaveTranslation() {
		val text = TranslatableText(German, "Baum")

		assertThat(text.hasTranslation(German), `is`(true))
		assertThat(text.hasTranslation(English), `is`(false))
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

		assertThat(text.getTranslation(), `is`(text.getTranslation(System.get().currentLanguage())))
	}

	@Test
	fun shouldFallbackToDefaultLanguage() {
		java.lang.System.setProperty("user.language", "de")
		val text = TranslatableText(English, "Tree")

		assertThat(text.getTranslation(), `is`("Tree"))
	}

	@Test
	fun shouldFallbackToAnyLanguageWithMissingDefault() {
		java.lang.System.setProperty("user.language", English.code)
		val text = TranslatableText(German, "Baum")

		assertThat(text.getTranslation(), `is`("Baum"))
	}

	@Test
	fun shouldWriteAndRead() {
		val obj = ClassUsingTranslatable(TranslatableText(listOf(
			Translation(German, "Meer"),
			Translation(English, "Sea")
		)))
		val typeMap = TypeMapImpl()
		typeMap.register("foo", ClassUsingTranslatable::class)
		typeMap.register("translation", Translation::class)
		val storableCloner = StorableClonerJvm(typeMap)

		val clone = storableCloner.clone(obj) as ClassUsingTranslatable

		assertThat(clone.attribute.getTranslation(German), `is`("Meer"))
		assertThat(clone.attribute.getTranslation(English), `is`("Sea"))
	}

	@Test
	fun shouldBeEqualWithSameTranslations() {
		val text1 = TranslatableText("Text")
		val text2 = TranslatableText("Text")
		assertThat(text1, `is`(text2))
	}

	@Test
	fun shouldBeDifferentWithDifferentTranslations() {
		val text1 = TranslatableText("Text")
		val text2 = TranslatableText("Text2")
		assertThat(text1, `is`(not(text2)))
	}

	@Test
	fun shouldBeImmutable() {
		val text1 = TranslatableText("Text")
		val text2 = text1.withTranslation("Text2")
		assertThat(text1, not(sameInstance(text2)))
	}

	@Test
	fun shouldBeEmpty() {
		val text = TranslatableText()
		assertThat(text.isEmpty, `is`(true))
	}

	@Test
	fun shouldTranslateOptionally() {
		java.lang.System.setProperty("user.language", "en")
		assertThat(TranslatableText().getOptionalTranslation(), `is`(nullValue()))
		assertThat(TranslatableText(English, "Tree").getOptionalTranslation(), `is`("Tree"))
	}
}

class ClassUsingTranslatable(text: TranslatableText? = null) : Storable {

	var attribute: TranslatableText = text ?: TranslatableText()
		private set

	override var storableId: Int = 0

	override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) { }

	override fun getStorableChildren(): Iterator<Storable> {
		return Collections.emptyIterator()
	}

	override fun write(writer: StoreWriter) {
		writer.writeStorables("name", attribute.allTranslations())
	}

	override fun read(reader: StoreReader) {
		attribute = TranslatableText(reader.readStorables("name"))
	}
}
