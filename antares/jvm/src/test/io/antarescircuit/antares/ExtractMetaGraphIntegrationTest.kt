package io.antarescircuit.antares

import io.antarescircuit.antares.model.AntaresGraphTypes
import io.antarescircuit.antares.model.signal.DigitalSignal
import io.antarescircuit.antares.view.inout.DigitalCircuitInOutView
import io.antarescircuit.antares.view.input.SwitchView
import io.antarescircuit.antares.view.metagraph.AntaresMetaGraphService
import io.antarescircuit.antares.view.output.LEDView
import io.antarescircuit.jabbah.base.geom.Point2D
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.getDrawableInstances
import io.antarescircuit.jabbah.edit.Component
import io.antarescircuit.jabbah.edit.DrawingView
import io.antarescircuit.jabbah.edit.DrawingViewMockBuilder
import io.antarescircuit.jabbah.edit.model.text.TranslatableText
import io.antarescircuit.jabbah.graph.MetaGraph
import io.antarescircuit.jabbah.graph.container.ContainerDrawingLayouter
import io.antarescircuit.jabbah.graph.library.LibraryModule
import io.antarescircuit.jabbah.graph.view.EdgeView
import io.antarescircuit.jabbah.graph.view.GraphView
import io.antarescircuit.jabbah.graph.view.GraphViewBuilder
import io.antarescircuit.jabbah.graph.view.graph.GraphViewCopyPasteService
import io.antarescircuit.jabbah.graph.view.module.GraphViewModule
import kotlin.test.Test
import kotlin.test.assertEquals

class ExtractMetaGraphIntegrationTest : AbstractJvmCircuitTest()  {

	private lateinit var sourceMetaGraph: MetaGraph
	private lateinit var switchView: SwitchView
	private lateinit var ledView: LEDView
	private lateinit var ledView2: LEDView
	private lateinit var edgeView: EdgeView<DigitalSignal>

	private val drawingViewBuilder = DrawingViewMockBuilder()
		.withSize(1000, 1000)
		.withDrawingAccessor(::getCircuitView)

	private val library get() = LibraryModule.libraryHolder.library

	override fun getCircuitView(): GraphView = sourceMetaGraph.graph.graphView

	override fun setup() {
		super.setup()
		GraphViewModule.metaGraphService = AntaresMetaGraphService(copyPasteService = GraphViewCopyPasteService())
		BaseModule.properties.set(ContainerDrawingLayouter.PROP_CONTAINER_DRAWING_LAYOUTER, ContainerDrawingLayouter.Narrow.customName)

		setupLibrary()
		val builder = GraphViewBuilder<DigitalSignal>("test")

		switchView = builder.addVerticeView(SwitchView().also { it.location = Point2D(100, 100) })
		ledView = builder.addVerticeView(LEDView().also { it.location = Point2D(200, 100) })
		edgeView = builder.connect(switchView, ledView)

		// The component NOT to be extracted
		ledView2 = builder.addVerticeView(LEDView().also { it.location = Point2D(200, 200) })

		val libraryBuilder = TestLibraryBuilder()
		sourceMetaGraph = libraryBuilder.addGraphView(builder.graphView, library)
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
			TranslatableText("Extract"),
			AntaresGraphTypes.Digital,
			drawingViewBuilder.build<Component>() as DrawingView<GraphView>,
			componentIds,
			library
		)
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