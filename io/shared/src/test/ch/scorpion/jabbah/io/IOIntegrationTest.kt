package ch.scorpion.jabbah.io

import ch.scorpion.jabbah.base.collection.EmptyIterator
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
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

    @Before
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

		assertThat(A.instancesCount, `is`(0))
		assertThat(b.name, `is`("anyB"))
	}

    private fun checkDocument(doc: Document) {
        assertThat(doc.children.size, `is`(2))
        assertTrue { doc.children[0] is A }
        assertTrue { doc.children[1] is B }

        val a = doc.children[0] as A

        assertThat(a.aString, `is`("test"))
        assertThat(a.aDouble, `is`(3.5))
        assertThat(a.aBoolean, `is`(true))
        assertThat(a.aLong, `is`(123L))
        assertThat(a.childB.size, `is`(3))
        assertThat(a.childB[0].name, `is`("b1"))
        assertThat(a.childB[1].name, `is`("b2"))
        assertThat(a.childB[2].name, `is`("b3"))
        assertThat(a.referencedB!!.name, `is`("anyB"))
    }

    class Document : Storable {

        override var storableId: Int = 0

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

        override var storableId: Int = 0
	    var aString: String = ""
        var aInt: Int= 0
        var aDouble: Double = 0.0
        var aBoolean: Boolean = false
        var aLong: Long = 0
        var childB = mutableListOf<B>()
        var referencedB: B? = null

        override fun resolve(reference: Reference, referenceResolver: ReferenceResolver) {
            if (reference.name == "referencedB") {
                referencedB = referenceResolver.getStorable(reference.referenceId) as B
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
            for (b in reader.readStorables("childB")) {
                childB.add(b as B)
            }
            reader.requestResolution(this, Reference(
                name = "referencedB",
                referenceId = reader.readInt("referencedB")
            ))
        }

        override fun getStorableChildren(): Iterator<Storable> = EmptyIterator()
    }

    class B(var name: String = "") : Storable {
        override var storableId: Int = 0

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