package ch.scorpion.antares

import ch.scorpion.jabbah.base.Language
import ch.scorpion.jabbah.base.Translations
import java.util.*
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class TranslationConsistencyTest {

    private var isConsistent = true

    @BeforeTest
    fun setup() {
        AntaresTestRule.configure()
    }

    @Test
    fun shouldHaveConsistentTranslations() {
        for (bundleName in Translations.bundleNames) {
            val englishKeys = ResourceBundle.getBundle(bundleName, Locale(Language.English.code)).keys.asSequence().toSet()
            val germanKeys = ResourceBundle.getBundle(bundleName, Locale(Language.German.code)).keys.asSequence().toSet()

            for (key in englishKeys) {
                if (!germanKeys.contains(key)) {
                    missingKey(Language.German, bundleName, key)
                }
            }
        }

        assertTrue(isConsistent)
    }

    private fun missingKey(language: Language, bundleName: String, key: String) {
        println("${language.code}-$bundleName: Missing key '$key'")
        isConsistent = false
    }
}