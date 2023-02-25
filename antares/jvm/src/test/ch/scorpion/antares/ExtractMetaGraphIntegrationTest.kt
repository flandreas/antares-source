package ch.scorpion.antares

import ch.scorpion.antares.model.AntaresGraphTypes
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.input.SwitchView
import ch.scorpion.antares.view.metagraph.AntaresMetaGraphService
import ch.scorpion.antares.view.output.LEDView
import ch.scorpion.jabbah.base.geom.Point2D
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.draw.getDrawableInstances
import ch.scorpion.jabbah.edit.DrawingView
import ch.scorpion.jabbah.edit.model.text.TranslatableText
import ch.scorpion.jabbah.graph.MetaGraph
import ch.scorpion.jabbah.graph.container.ContainerDrawingLayouter
import ch.scorpion.jabbah.graph.library.LibraryModule
import ch.scorpion.jabbah.graph.view.EdgeView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.GraphViewBuilder
import ch.scorpion.jabbah.graph.view.graph.GraphViewCopyPasteService
import ch.scorpion.jabbah.graph.view.module.GraphViewModule
import io.mockk.every
import io.mockk.mockk
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtractMetaGraphIntegrationTest : AbstractJvmCircuitTest()  {

	companion object {
		init {
			AntaresTestRule.configure()
			GraphViewModule.metaGraphService = AntaresMetaGraphService(GraphViewCopyPasteService())
			BaseModule.properties.set(ContainerDrawingLayouter.PROP_CONTAINER_DRAWING_LAYOUTER, ContainerDrawingLayouter.Narrow.customName)
		}
	}

	private lateinit var sourceMetaGraph: MetaGraph
	private lateinit var switchView: SwitchView
	private lateinit var ledView: LEDView
	private lateinit var ledView2: LEDView
	private lateinit var edgeView: EdgeView<DigitalSignal>

	private val drawingView = mockk<DrawingView<GraphView>>()

	private val library get() = LibraryModule.libraryHolder.library

	override fun getCircuitView(): GraphView = sourceMetaGraph.graph.graphView

	@BeforeTest
	fun setupCircuit() {
		setupLibrary()
		val builder = GraphViewBuilder<DigitalSignal>("test")

		switchView = builder.addVerticeView(SwitchView().also { it.location = Point2D(100, 100) })
		ledView = builder.addVerticeView(LEDView().also { it.location = Point2D(200, 100) })
		edgeView = builder.connect(switchView, ledView)

		// The component NOT to be extracted
		ledView2 = builder.addVerticeView(LEDView().also { it.location = Point2D(200, 200) })

		val libraryBuilder = TestLibraryBuilder()
		sourceMetaGraph = libraryBuilder.addGraphView(builder.graphView, library)

		every { drawingView.drawing }.answers {getCircuitView() }
	}

	@Test
	fun shouldExtract() {
		val targetMetaGraph = extract()

		assertEquals(2, getCircuitView().drawables.size)
		assertEquals(3, targetMetaGraph.graph.graphView.drawables.size)
	}

	private fun extract(): MetaGraph {
		val componentIds = listOf(switchView.id, edgeView.id, ledView.id)
		val uuid = GraphViewModule.metaGraphService.extractMetaGraph(
			TranslatableText("Extract"), AntaresGraphTypes.Digital, drawingView, componentIds, library)
		return library.getMetaGraph(uuid)
	}

	@Test
	fun shouldReplaceSwitchesAndLEDs() {
		val targetMetaGraph = extract()

		assertEquals(0, targetMetaGraph.graph.graphView.getDrawableInstances<SwitchView>().size)
		assertEquals(0, targetMetaGraph.graph.graphView.getDrawableInstances<LEDView>().size)
		assertEquals(2, targetMetaGraph.graph.graphView.getDrawableInstances<DigitalCircuitInOutView>().size)
	}
}