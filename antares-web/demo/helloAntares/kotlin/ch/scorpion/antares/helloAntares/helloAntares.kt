package ch.scorpion.antares.helloAntares

import ch.scorpion.antares.module.AntaresModuleJs
import ch.scorpion.jabbah.base.LOG_SYSTEM
import ch.scorpion.jabbah.base.LogLevel
import ch.scorpion.jabbah.base.LogSystemJs
import ch.scorpion.jabbah.base.StringUtils
import ch.scorpion.jabbah.draw.style.StyleRepository
import ch.scorpion.jabbah.draw.view.CanvasJs
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Drawing
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.editor.EditEditorModule
import ch.scorpion.jabbah.edit.view.DrawingViewImpl
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.container.ContainerDrawing
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import ch.scorpion.jabbah.io.DomXmlReader
import ch.scorpion.jabbah.io.StoreXmlReader
import org.w3c.dom.Document
import org.w3c.dom.HTMLInputElement
import org.w3c.xhr.DOCUMENT
import org.w3c.xhr.XMLHttpRequest
import org.w3c.xhr.XMLHttpRequestResponseType
import kotlin.browser.document
import kotlin.properties.Delegates

var editor by Delegates.notNull<Editor>()

/**
 * Loads an antares [GraphView] from the REST API and displays it.
 */
@Suppress("unused")
fun hello() {
    AntaresModuleJs.require()

    LOG_SYSTEM!!.getLogger(ContainerDrawing::class).value.setLogLevel(LogLevel.DEBUG)

    LibraryModule.libraryHolder.library.load()

    val drawing = GraphViewImpl<GraphElementView<*>>()

    val canvas = CanvasJs("kotlinCanvas", { DrawingViewImpl(drawing as Drawing<Component>, it) }, StyleRepository.INSTANCE )
    editor = EditEditorModule.createEditor(canvas.view as DrawingView<Drawing<Component>>)
    editor.view.editable = false

    canvas.repaint()
}

@Suppress("unused")
fun handleLoad() {

    val input = document.getElementById("fileName") as HTMLInputElement
    val fileName = input.value

    if (StringUtils.isEmpty(fileName)) {
        js("alert('Enter a file name')")
        return
    }

    val request = XMLHttpRequest()

    request.open("GET", "http://localhost:4567/jabbah-graph/graphView/$fileName", true)
    request.responseType = XMLHttpRequestResponseType.DOCUMENT
    request.overrideMimeType("text/xml")
    request.onload = {
        console.log(request.response)
        handleResponse(request.responseXML!!)
    }
    request.send()

}

private fun handleResponse(doc: Document) {
    val reader = DomXmlReader(doc)
    val storeXmlReader = StoreXmlReader(reader)

    val metaGraph = storeXmlReader.readStorable() as MetaGraph

    editor.view.drawing = metaGraph.graph!!.graphView!! as Drawing<Component>
}
