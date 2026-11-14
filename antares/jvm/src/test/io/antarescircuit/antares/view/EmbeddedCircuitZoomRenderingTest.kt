package io.antarescircuit.antares.view

import dev.mokkery.MockMode
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.mock
import io.antarescircuit.antares.AntaresTestRule
import io.antarescircuit.antares.TestCircuitBuilder
import io.antarescircuit.antares.TestLibraryBuilder
import io.antarescircuit.antares.view.gate.LogicGateView
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.jabbah.base.event.EventBusImpl
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.geom.Direction
import io.antarescircuit.jabbah.graph.container.InternalLabelOrientation
import io.antarescircuit.jabbah.graph.view.port.AbstractPortView
import io.antarescircuit.jabbah.base.time.SystemSpeed
import io.antarescircuit.jabbah.draw.DrawContext
import io.antarescircuit.jabbah.draw.graphics.Graphics2DJvm
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.edit.snap.GridImpl
import io.antarescircuit.jabbah.execution.speed.CurrentSystemSpeedCategory
import io.antarescircuit.jabbah.graph.GraphApplicationContext
import io.antarescircuit.jabbah.graph.library.*
import io.antarescircuit.jabbah.graph.model.Vertice
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphDisplayMode
import io.antarescircuit.jabbah.graph.view.vertice.SubGraphVerticeViewImpl
import java.awt.Color
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import javax.swing.SwingUtilities
import kotlin.test.Test
import kotlin.test.assertEquals

class EmbeddedCircuitZoomRenderingTest {
	@Test
	fun shouldRenderNativeAndShrunkCircuitsWithUnscaledLabels() {
		SwingUtilities.invokeAndWait { renderComparison() }
		SwingUtilities.invokeAndWait { }
	}

	@Test
	fun shouldRenderNorthPortLabelsAboveTheTitle() {
		SwingUtilities.invokeAndWait { renderComparison(northPorts = true) }
		SwingUtilities.invokeAndWait { }
	}

	private fun renderComparison(northPorts: Boolean = false) {
		AntaresTestRule.configure()
		val savedHolder = LibraryModule.libraryHolder
		val savedImages = EditModule.imageRepository
		val savedPersistence = LibraryModule.userLibraryPersistenceService
		try {
			val library = LibraryImpl(TranslatableText("test"))
			val events = EventBusImpl()
			LibraryModule.libraryHolder = LibraryHolderImpl(library, events)
			val persistence = mock<LibraryPersistenceService>(MockMode.autofill)
			LibraryModule.userLibraryPersistenceService = persistence
			val circuit = TestCircuitBuilder("Half Adder")
			val a = circuit.addInput("A").also { it.location = Point2D(-140, -28) }
			val b = circuit.addInput("B").also { it.location = Point2D(-140, 84) }
			val sum = circuit.addOutput("S").also { it.location = Point2D(210, 0) }
			val carry = circuit.addOutput("C").also { it.location = Point2D(210, 140) }
			if (northPorts) {
				a.orientation = Direction.SOUTH
				b.orientation = Direction.SOUTH
				sum.orientation = Direction.NORTH
				carry.orientation = Direction.NORTH
				a.name = "A input"
				b.name = "B input"
				sum.name = "Sum output"
				carry.name = "Carry output"
			}
			val xor = circuit.addVerticeView(LogicGateView.xorGateView())
			val and = circuit.addVerticeView(LogicGateView.andGateView()).also { it.location = Point2D(0, 140) }
			val aEdge = circuit.connect(a, xor, xor.model.getInput(1))
			val bEdge = circuit.connect(b, xor, xor.model.getInput(2))
			circuit.split(aEdge, 0, Point2D(-98, -28), and.getPortView(and.model.getInput<DigitalSignal>(1)))
			circuit.split(bEdge, 0, Point2D(-70, 84), and.getPortView(and.model.getInput<DigitalSignal>(2)))
			circuit.connect(xor, sum)
			circuit.connect(and, carry)
			val meta = TestLibraryBuilder(libraryService = LibraryService(eventBus = events)).addGraphView(circuit.graphView, library)
			every { persistence.loadMetaGraph(any(), meta.uuid) } returns meta
			val vertex = library.getContainerLibraryElement(meta.uuid)!!.getNewInstance<Vertice>() as SubGraphVerticeViewImpl
			val parent = TestCircuitBuilder("Parent")
			val grid = GridImpl(chosenDistance = 7.0)
			parent.graphView.snapper = grid
			parent.add(vertex)
			if (northPorts) vertex.getPortViews().forEach { (it as AbstractPortView<*>).internalLabelOrientation = InternalLabelOrientation.Aligned }
			vertex.displayMode = SubGraphDisplayMode.GRAPH
			vertex.location = Point2D(28, 28)
			val image = BufferedImage(1000, if (northPorts) 650 else 450, BufferedImage.TYPE_INT_RGB)
			val awt = image.createGraphics()
			try {
				awt.color = Color.WHITE
				awt.fillRect(0, 0, image.width, image.height)
				awt.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
				val transform = awt.transform
				val context = DrawContext(Graphics2DJvm(awt), appContext = GraphApplicationContext(CurrentSystemSpeedCategory(SystemSpeed())))
				vertex.draw(context)
				vertex.setFrame(588.0, 28.0, vertex.shape.width * 0.6, vertex.shape.height * 0.6)
				vertex.draw(context)
				assertEquals(transform, awt.transform)
				val file = File(if (northPorts) "build/embedded-circuit-north-labels.png" else "build/embedded-circuit-zoom.png")
				file.parentFile.mkdirs()
				ImageIO.write(image, "png", file)
			} finally {
				awt.dispose()
				vertex.dispose()
				grid.dispose()
			}
		} finally {
			LibraryModule.libraryHolder.l?.dispose()
			LibraryModule.libraryHolder = savedHolder
			EditModule.imageRepository = savedImages
			LibraryModule.userLibraryPersistenceService = savedPersistence
		}
	}
}
