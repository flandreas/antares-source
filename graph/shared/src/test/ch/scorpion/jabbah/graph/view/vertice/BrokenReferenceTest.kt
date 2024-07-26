package ch.scorpion.jabbah.graph.view.vertice

import ch.scorpion.jabbah.base.LongValueImpl
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.TestLibraryBuilder
import ch.scorpion.jabbah.graph.library.LibraryImpl
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.library.LibraryService
import ch.scorpion.jabbah.graph.library.MemoryLibraryPersistenceService
import ch.scorpion.jabbah.graph.model.param.GraphParamTypeRegistry
import ch.scorpion.jabbah.graph.model.param.GraphParamValue
import ch.scorpion.jabbah.graph.model.param.LongValueGraphParamType
import ch.scorpion.jabbah.graph.model.vertice.SubGraphVerticeRef
import ch.scorpion.jabbah.graph.view.GraphViewTestRule
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class BrokenReferenceTest {

    companion object {
        init {
            GraphViewTestRule.configure()
        }
    }

    @BeforeTest
    fun setup() {
        LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
        LibraryModule.libraryService = LibraryService()
        LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))

        GraphParamTypeRegistry.clear()
        GraphParamTypeRegistry.register(LongValueGraphParamType.name) { LongValueGraphParamType }
    }

    @Test
    fun shouldLoadMetaGraphWithBrokenReference() {
        val library = LibraryModule.libraryHolder.library
        val builder = TestLibraryBuilder()
        val inner = builder.addInnerCustomComponent(library)
        val graphParamValue = GraphParamValue.create("P", LongValueGraphParamType, LongValueImpl(42L), null)
        val outer = builder.addOuterCustomComponent(library, paramValue = graphParamValue)

        LibraryModule.libraryService.removeLibraryItem(library, library.getContainerLibraryElement(inner.uuid)!!)
        val outerCle = library.getContainerLibraryElement(outer.uuid)!!
        LibraryModule.libraryService.loadMetaGraph(library, outerCle)

        val metaGraph = outerCle.metaGraph!!
        val vv = metaGraph.graph.graphView.getDrawables { it is SubGraphVerticeView }.first()

        assertTrue((vv.model as SubGraphVerticeRef).isBroken)
    }
}