package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import kotlin.test.Test
import kotlin.test.BeforeTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * An integration tests that tests the interaction of various classes of the [ch.scorpion.jabbah.io] package.
 */
class IOIntegrationTest {

    private val xml =
        "<document _id='1'>" +
            "<children>" +
                "<a _id='1' aString='test' aInt='42' aDouble='3.5' aBoolean='true' aLong='123' referencedB='2'>" +
                "<childB>" +
                    "<b name='b1'/>" +
                    "<b name='b2'/>" +
                    "<b name='b3'/>" +
                "</childB>" +
                "</a>" +
                "<b _id='2' name='anyB'/>" +
            "</children>" +
        "</document>"

    private lateinit var typeMap: TypeMap

    @BeforeTest
    fun setup() {
        BaseModuleJvm.require()
        typeMap = TypeMapImpl()
        typeMap.register("document", Document::class)
        typeMap.register("a", A::class)
        typeMap.register("b", B::class)
	    A.instancesCount = 0
    }

    @Test
    fun shouldRead () {
        val storeXmlReader = StoreXmlReader(
            ElectricXmlReader(ByteArrayInputStream(xml.toByteArray())),
            typeMap,
            SystemStorableCreator(),
            ReferenceResolverImpl())
        checkDocument(storeXmlReader.readStorable() as Document)
    }

    @Test
    fun shouldWrite() {
        val document = Document()
        val b = B("anyB")
        val a = A()
        a.aString = "test"
        a.aInt = 42
        a.aDouble = 3.5
        a.aBoolean = true
        a.aLong = 123
        a.childB.add(B("b1"))
        a.childB.add(B("b2"))
        a.childB.add(B("b3"))
        a.referencedB = b
        document.children.add(a)
        document.children.add(b)

        val buffer = ByteArrayOutputStream()
        val xmlWriter = ElectricXmlWriter(buffer)

        val storeXmlWriter = StoreXmlWriter(xmlWriter, typeMap, GlobalIdentityCreator()) {true}
	    storeXmlWriter.writeStorable(document)

        // Read in order to check
        val storeXmlReader = StoreXmlReader(
            ElectricXmlReader(ByteArrayInputStream(buffer.toByteArray())),
            typeMap,
            SystemStorableCreator(),
            ReferenceResolverImpl())
        checkDocument(storeXmlReader.readStorable() as Document)
    }

	@Test
	fun shouldReadElementAtPath() {
		val xml =
			"<document _id='1'>" +
				"<myA>" +
					"<a _id='1' aString='test' aInt='42' aDouble='3.5' aBoolean='true' aLong='123' referencedB='2'/>" +
				"</myA>" +
				"<myB>" +
					"<b _id='2' name='anyB'/>" +
				"</myB>" +
			"</document>"

		val storeXmlReader = StoreXmlReader(
			ElectricXmlReader(ByteArrayInputStream(xml.toByteArray())),
			typeMap,
			SystemStorableCreator(),
			ReferenceResolverImpl())

		val b = storeXmlReader.readStorable(listOf("myB")) as B

		assertEquals(A.instancesCount, 0)
		assertEquals(b.name, "anyB")
	}

    private fun checkDocument(doc: Document) {
        assertEquals(2, doc.children.size)
        assertTrue(doc.children[0] is A)
        assertTrue(doc.children[1] is B)

        val a = doc.children[0] as A

        assertEquals("test", a.aString)
        assertEquals( 3.5, a.aDouble)
        assertTrue(a.aBoolean)
        assertEquals(123L, a.aLong)
        assertEquals(3, a.childB.size)
        assertEquals("b1", a.childB[0].name)
        assertEquals("b2", a.childB[1].name)
        assertEquals("b3", a.childB[2].name)
        assertEquals("anyB", a.referencedB!!.name)
    }

    class Document : Storable {

        override var storableId: Int = Storable.UNDEFINED_ID

        val children = mutableListOf<Storable>()

        override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
            // empty
        }

        override fun write(writer: StoreWriter) {
            writer.writeStorables("children", getStorableChildren())
        }

        override fun read(reader: StoreReader) {
            children.clear()
            children += reader.readStorables("children")
        }

        override fun getStorableChildren(): Iterator<Storable> {
            return children.iterator()
        }
    }

    class A : Storable {

	    companion object {
	        var instancesCount: Int = 0
	    }

	    init {
		    instancesCount++
	    }

        override var storableId: Int = Storable.UNDEFINED_ID
	    var aString: String = ""
        var aInt: Int= 0
        var aDouble: Double = 0.0
        var aBoolean: Boolean = false
        var aLong: Long = 0
        var childB = mutableListOf<B>()
        var referencedB: B? = null

        override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
            if (reference.name == "referencedB") {
                referencedB = referenceResolver.getStorable(reference.referenceId)
            }
        }

        override fun write(writer: StoreWriter) {
            writer.writeString("aString", aString)
            writer.writeInt("aInt", aInt)
            writer.writeDouble("aDouble", aDouble)
            writer.writeBoolean("aBoolean", aBoolean)
            writer.writeLong("aLong", aLong)
            writer.writeStorables("childB", childB.iterator())
            referencedB?.let {
                writer.writeInt("referencedB", writer.provideIdentity(it))
            }
        }

        override fun read(reader: StoreReader) {
            aString = reader.readString("aString")
            aInt = reader.readInt("aInt")
            aDouble = reader.readDouble("aDouble")
            aBoolean = reader.readBoolean("aBoolean")
            aLong = reader.readLong("aLong")
            childB.clear()
            for (b in reader.readStorables<B>("childB")) {
                childB.add(b)
            }
            reader.requestResolution(this, Reference(
                name = "referencedB",
                referenceId = reader.readInt("referencedB")
            ))
        }

        override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()
    }

    class B(var name: String = "") : Storable {
        override var storableId: Int = Storable.UNDEFINED_ID

        override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
            // empty
        }

        override fun write(writer: StoreWriter) {
            writer.writeString("name", name)
        }

        override fun read(reader: StoreReader) {
            name = reader.readString("name")
        }

        override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()
    }
}