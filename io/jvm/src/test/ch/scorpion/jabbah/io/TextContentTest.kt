package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.junit.Before
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class TextContentTest {

    private val typeMap = TypeMapImpl()

    @Before
    fun setup() {
        BaseModuleJvm.require()
        typeMap.register("testStorable", TestStorable::class)
    }

    @Ignore // String formatting is not correct
    @Test
    fun shouldWriteTextContent() {
        val buffer = ByteArrayOutputStream()
        val xmlWriter = ElectricXmlWriter(buffer)
        val storeXmlWriter = StoreXmlWriter(xmlWriter, typeMap, GlobalIdentityCreator())

        storeXmlWriter.writeStorable(TestStorable("This is XML text content."))
        buffer.flush()

        assertEquals("""
            <?xml version='1.0' encoding='UTF-8'?>
            <testStorable _id='0'>
              <documentation>
            This is XML text content.
            </documentation>
            </testStorable>
        """.trimIndent().replace("\n", "\r\n"), buffer.toString())
    }

    @Test
    fun shouldReadTextContent() {
        val buffer = ByteArrayInputStream("""
            <?xml version='1.0' encoding='UTF-8'?>
            <testStorable _id='0'>
              <documentation>
                This is XML text content.
              </documentation>
            </testStorable>            
        """.trimIndent().toByteArray())
        val xmlReader = ElectricXmlReader(buffer)
        val storeXmlReader = StoreXmlReader(xmlReader, typeMap, ReferenceResolverImpl())

        val storable = storeXmlReader.readStorable<TestStorable>()

        assertEquals("This is XML text content.", storable.documentation)
    }

    @Test
    fun shouldReadAndWriteTextContent() {
        val buffer = ByteArrayOutputStream()
        val xmlWriter = ElectricXmlWriter(buffer)
        val storeXmlWriter = StoreXmlWriter(xmlWriter, typeMap, GlobalIdentityCreator())

        storeXmlWriter.writeStorable(TestStorable("This is XML text content."))
        buffer.flush()

        val xmlReader = ElectricXmlReader(ByteArrayInputStream(buffer.toByteArray()))
        val storeXmlReader = StoreXmlReader(xmlReader, typeMap, ReferenceResolverImpl())

        val storable = storeXmlReader.readStorable<TestStorable>()

        assertEquals("This is XML text content.", storable.documentation)
    }

    class TestStorable(
        documentation: String? = null
    ) : AbstractStorable() {

        var documentation: String? = documentation
            private set

        override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {}

        override fun write(writer: StoreWriter) {
            documentation?.let {
                writer.writeText("documentation", it)
            }
        }

        override fun read(reader: StoreReader) {
            if (reader.hasElement("documentation")) {
                documentation = reader.readText("documentation")
            }
        }
    }
}