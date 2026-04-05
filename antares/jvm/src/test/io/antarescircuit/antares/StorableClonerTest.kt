package io.antarescircuit.antares

import io.antarescircuit.jabbah.io.StorableCloner
import junit.framework.TestCase.assertEquals
import kotlin.test.BeforeTest
import kotlin.test.Test

class StorableClonerTest {

    companion object {
        private val DATA = """
            <?xml version='1.0' encoding='UTF-8'?>
            <project _id='0' appVersion='0.0.0' defaultElement='022e819f-445a-4c1e-8deb-2ce0149e2b62' uuid='4346eb30-7da5-40b1-8614-e55ae794c89c' author='5ecf330b-e395-4e17-88b0-0883834b384a' imports='6707f981-110d-4629-a0bf-c35a4688025c' visibility='public'>
              <folder>
                <libraryFolder _id='1'>
                  <name>
                    <translation lang='en' text='Upload'/>
                  </name>
                  <items>
                    <containerLibraryElement _id='2' uuid='022e819f-445a-4c1e-8deb-2ce0149e2b62' type='digital'>
                      <name>
                        <translation lang='en' text='New Circuit'/>
                      </name>
                    </containerLibraryElement>
                  </items>
                </libraryFolder>
              </folder>
              <desc>
                <translation lang='en' text='Test project to be uploaded to Akrab'/>
              </desc>
            </project>
        """.trimIndent()
    }

    @BeforeTest
    fun setup() {
        AntaresTestRule.configure()
    }

    @Test
    fun shouldSerialize() {
        val storable = StorableCloner.deserialize(DATA)
        val output = StorableCloner.serialize(storable)
        assertEquals(DATA.replace("\n", "\r\n"), output)
    }
}