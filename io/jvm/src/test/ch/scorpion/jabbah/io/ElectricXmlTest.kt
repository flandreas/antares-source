package ch.scorpion.jabbah.io

import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.assertTrue

class ElectricXmlTest {

    @Test
    fun shouldEscapeSpecialChar() {
        val out = ByteArrayOutputStream()
        val writer = ElectricXmlWriter(out)
        writer.addElementAndDescend("elem")
        writer.setAttributeValue("name", "<value>")
        writer.flush()
        assertTrue(String(out.toByteArray()).contains("<elem name='&lt;value&gt;'/>"))
    }

    /**
     * Electric XML unfortunately does NOT encode newlines in XML attributes and is therefore not
     * DOM API compliant. Reading them back with Electric XML is okay, but reading back with
     * a compliant XML parser will replace the newline with spaces, also according to the specs.
     * https://stackoverflow.com/questions/2004386/how-to-save-newlines-in-xml-attribute (answer by Tomalak).
     */
    @Test
    fun doesNotEncodeNewline() {
        val out = ByteArrayOutputStream()
        val writer = ElectricXmlWriter(out)
        writer.addElementAndDescend("elem")
        writer.setAttributeValue("name", "line1\nline2")
        writer.flush()
        assertTrue(String(out.toByteArray()).contains("<elem name='line1\nline2'/>"))
    }

    /**
     * This test demonstrates that newlines in attributes cannot be represented
     * by '&xA;' because the ampersand is encoded separately instead of recognized as part of an encoding.
     */
    @Test
    fun cannotEncodeNewlineManually() {
        val out = ByteArrayOutputStream()
        val writer = ElectricXmlWriter(out)
        writer.addElementAndDescend("elem")
        writer.setAttributeValue("name", "line1&xA;line2")
        writer.flush()
        val text = String(out.toByteArray())
        assertTrue(text.contains("<elem name='line1&amp;xA;line2'/>"))
    }
}