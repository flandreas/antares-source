package ch.scorpion.jabbah.edit.model.text

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.edit.module.EditModule
import kotlin.test.*

class TranslatableTextPanelTest {

	@BeforeTest
	fun setup() {
		EditModule.require()
	}

	@Test
	fun germanUserShouldEditGermanAndEnglishText() {
		System.setProperty("user.language", "de")
		val panel = TranslatableTextPanel(TranslatableText())

		assertNotNull(panel.currentLangTextField.parent)
		assertNotNull(panel.alternativeLangTextField.parent)
	}

	@Test
	fun englishUserShouldOnlyEditEnglishIfAllIsEmpty() {
		System.setProperty("user.language", "en")
		val panel = TranslatableTextPanel(TranslatableText())

		assertNotNull(panel.currentLangTextField.parent)
		assertNull(panel.alternativeLangTextField.parent)
	}

	@Test
	fun englishUserShouldOnlyEditEnglishIfEnglishIsNotEmpty() {
		System.setProperty("user.language", "en")
		val panel = TranslatableTextPanel(TranslatableText(Language.English, "Tree"))

		assertNotNull(panel.currentLangTextField.parent)
		assertNull(panel.alternativeLangTextField.parent)
	}

	@Test
	fun englishUserShouldSeeGermanIfEnglishIsEmpty() {
		System.setProperty("user.language", "en")
		val panel = TranslatableTextPanel(TranslatableText(Language.German, "Baum"))

		assertNotNull(panel.currentLangTextField.parent)
		assertNotNull(panel.alternativeLangTextField.parent)
	}
}