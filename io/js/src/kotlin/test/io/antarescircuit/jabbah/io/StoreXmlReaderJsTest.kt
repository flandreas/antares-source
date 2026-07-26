package io.antarescircuit.jabbah.io

import io.antarescircuit.jabbah.io.module.IOModuleJs
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class StoreXmlReaderJsTest {

    private lateinit var typeMap: TypeMap

    @BeforeTest
    fun setup() {
        IOModuleJs.require()
        typeMap = TypeMapImpl()
        typeMap.register("string", StringStorable::class)
    }

    @Test
    fun shouldReadMap() {
        val reader = DomXmlReader("""
            <data>
                <content>
                    <antares.model.gate.defaultPropagationDelay>
                        <string content='20'/>
                    </antares.model.gate.defaultPropagationDelay>
                </content>
            </data>
        """.trimIndent())
        val storeReader = StoreXmlReader(reader, typeMap)

        val map = storeReader.readMap("content")

        assertEquals("20", (map["antares.model.gate.defaultPropagationDelay"] as StringStorable).content)
    }
}