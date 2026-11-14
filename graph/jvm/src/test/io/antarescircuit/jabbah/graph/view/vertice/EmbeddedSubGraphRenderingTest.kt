package io.antarescircuit.jabbah.graph.view.vertice

import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.edit.model.rectangle.RectangleComponent
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.TestLibraryBuilder
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.GraphElementViewWrapper
import io.antarescircuit.jabbah.graph.view.GraphViewTestRule
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.test.Test
import kotlin.test.assertEquals
class EmbeddedSubGraphRenderingTest {

	@Test
	fun shouldRenderNativeScaleContentAndRestoreGraphicsTransform() {
		GraphViewTestRule.configure()
		LibraryModule.userLibraryPersistenceService = MemoryLibraryPersistenceService()
		LibraryModule.libraryHolder.l = LibraryImpl(TranslatableText("test"))
		val library = LibraryModule.libraryHolder.library
		val meta = TestLibraryBuilder().addInnerCustomComponent(library, "Embedded graph")
		val inner = meta.graph.graphView
		inner.add(GraphElementViewWrapper(RectangleComponent(10.0, 10.0, 100.0, 70.0).also { it.text = TranslatableText("Inner content") }))
		inner.getGraphPortView("I")!!.location = Point2D(-30, 40)
		inner.getGraphPortView("O")!!.location = Point2D(160, 40)
		val vertex = library.getContainerLibraryElement(meta.uuid)!!.getNewInstance<Vertice>() as SubGraphVerticeViewImpl
		vertex.displayMode = SubGraphDisplayMode.GRAPH
		vertex.location = Point2D(40, 40)
		val image = BufferedImage(400, 280, BufferedImage.TYPE_INT_RGB)
		val awt = image.createGraphics()
		try {
			awt.color = Color.WHITE
			awt.fillRect(0, 0, image.width, image.height)
			val transform = awt.transform
			vertex.draw(DrawContext(Graphics2DJvm(awt), appContext = GraphApplicationContext(CurrentSystemSpeedCategory(SystemSpeed()))))
			assertEquals(transform, awt.transform)
			val file = File("build/embedded-subgraph.png")
			file.parentFile.mkdirs()
			ImageIO.write(image, "png", file)
		} finally {
			awt.dispose()
			vertex.dispose()
		}
	}
}
