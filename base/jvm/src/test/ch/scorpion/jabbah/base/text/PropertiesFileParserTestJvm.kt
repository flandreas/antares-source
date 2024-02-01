package ch.scorpion.jabbah.base.text

import junit.framework.TestCase.assertTrue
import kotlin.test.Test

class PropertiesFileParserTestJvm {

    @Test
    fun shouldParseTranslationsFromResources() {
        PropertiesFileParserTestJvm::class.java.getResourceAsStream("/jabbah-base_en.properties").use {
            val properties = String(it.readAllBytes(), Charsets.UTF_8)
            val map = PropertiesFileParser.parse(properties)
            assertTrue(map.isNotEmpty())
        }
    }
}