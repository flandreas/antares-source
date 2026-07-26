package io.antarescircuit.jabbah.io

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DomXmlReaderTest {

    @Test
    fun shouldGetName() {
        val reader = DomXmlReader("""
            <content>
                <antares.model.gate.defaultPropagationDelay>
                    <string content='20'/>
                </antares.model.gate.defaultPropagationDelay>
            </content>            
        """.trimIndent())

        val name = reader.getName()

        assertEquals("content", name)
    }

    @Test
    fun shouldReadElementNames() {
        val reader = DomXmlReader(
            """
            <content>
                <antares.model.gate.defaultPropagationDelay>
                    <string content='20'/>
                </antares.model.gate.defaultPropagationDelay>
                <antares.model.undefinedInputBehaviour>
                    <string content='readAs0'/>
                </antares.model.undefinedInputBehaviour>
                <graph.model.allowedInconsistentNetDuration>
                    <string content='100'/>
                </graph.model.allowedInconsistentNetDuration>
                <io.antarescircuit.antares.model.input.Switch.defaultPropDelay>
                    <string content='1000'/>
                </io.antarescircuit.antares.model.input.Switch.defaultPropDelay>
            </content>
            """.trimIndent())

        val names = reader.getElementNames()

        assertTrue(names.contains("antares.model.gate.defaultPropagationDelay"))
    }
}