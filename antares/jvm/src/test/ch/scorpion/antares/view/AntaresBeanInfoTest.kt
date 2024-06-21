package ch.scorpion.antares.view

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.model.DigitalGraph
import ch.scorpion.antares.view.addressable.RAMView
import ch.scorpion.antares.view.addressable.RAMViewBeanInfo
import ch.scorpion.antares.view.addressable.ROMView
import ch.scorpion.antares.view.addressable.ROMViewBeanInfo
import ch.scorpion.antares.view.arithmetic.RandomView
import ch.scorpion.antares.view.arithmetic.RandomViewBeanInfo
import ch.scorpion.antares.view.gate.*
import ch.scorpion.antares.view.inout.DigitalCircuitInOutView
import ch.scorpion.antares.view.inout.DigitalCircuitInOutViewBeanInfo
import ch.scorpion.antares.view.input.*
import ch.scorpion.antares.view.net.*
import ch.scorpion.antares.view.output.*
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.view.GraphElementView
import ch.scorpion.jabbah.graph.view.GraphView
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import dev.mokkery.MockMode
import dev.mokkery.answering.calls
import dev.mokkery.answering.returns
import dev.mokkery.every
import dev.mokkery.matcher.any
import dev.mokkery.matcher.capture.Capture
import dev.mokkery.matcher.capture.capture
import dev.mokkery.matcher.capture.get
import dev.mokkery.mock
import org.junit.Test
import kotlin.test.Ignore

class AntaresBeanInfoTest {

	companion object {
		init {
			BaseModuleJvm.require()
			AntaresTestRule.configure()
		}
	}

	private val graph = DigitalGraph()
	private val drawing = mock<GraphView>(MockMode.autofill)
	private val commandManager = mock<CommandManager>(MockMode.autofill)
	private val view = mock<DrawingView<Drawing<Component>>>(MockMode.autofill)
	private val editor = mock<Editor>(MockMode.autofill)

	init {
		every { drawing.graph } returns graph
		every { editor.view } returns view
		every { editor.active } returns true
		every { editor.drawing } returns drawing as Drawing<Component>
		every { editor.commandManager } returns commandManager

		val command = Capture.slot<Command>()
		every { commandManager.beginTransaction(capture<Command>(command)) } calls {
			command.get().execute()
		}
	}

	private fun <T: GraphElementView<*>> readWrite(component: T, beanInfo: AbstractBeanInfo<T>) {
		every { drawing.getWithId(any()) } returns component
		every { drawing.getWidthIds(any()) } returns listOf(component)

		val properties = beanInfo.getProperties(component, editor)

		properties
			.forEach { it.readFromObject(component) }

		properties
			.filter { it.isEditable }
			.forEach {
				if (it is CommandPropertySwing<*>) {
					it.forceWriteToObject()
				} else {
					it.writeToObject(component)
				}
			}
	}

	// arithmetic

	@Test
	fun shouldReadWriteRandomView() {
		readWrite(RandomView(), RandomViewBeanInfo())
	}

	// gate

	@Test
	fun shouldReadWriteAndGateView() {
		readWrite(LogicGateView.andGateView(), LogicGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteBufferGateView() {
		readWrite(LogicGateView.bufferGateView(), LogicGateViewBeanInfo())
	}

	@Test
	fun shouldDelayGateView() {
		readWrite(DelayGateView(), DelayGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteNandGateView() {
		readWrite(LogicGateView.nandGateView(), LogicGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteNorGateView() {
		readWrite(LogicGateView.norGateView(), LogicGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteNotGateView() {
		readWrite(LogicGateView.notGateView(), LogicGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteOrGateView() {
		readWrite(LogicGateView.orGateView(), LogicGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteTriStateBufferGateView() {
		readWrite(TriStateBufferGateView(), TriStateBufferGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteXnorGateView() {
		readWrite(LogicGateView.xnorGateView(), LogicGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteXorGateView() {
		readWrite(LogicGateView.xorGateView(), LogicGateViewBeanInfo())
	}

	// inout

	@Test
	@Ignore
	fun shouldReadWriteCircuitInOutView() {
		readWrite(DigitalCircuitInOutView(), DigitalCircuitInOutViewBeanInfo())
	}

	// input

	@Test
	fun shouldReadWriteClockView() {
		readWrite(ClockView(), ClockViewBeanInfo())
	}

	@Test
	fun shouldReadWriteDipSwitchView() {
		readWrite(DipSwitchView(), DipSwitchViewBeanInfo())
	}

	@Test
	fun shouldReadWriteKeyboardView() {
		readWrite(KeyboardView(), KeyboardViewBeanInfo())
	}

	@Test
	fun shouldReadWriteSwitchView() {
		readWrite(SwitchView(), SwitchViewBeanInfo())
	}

	// memory

	@Test
	fun shouldReadWriteROMView() {
		readWrite(ROMView(), ROMViewBeanInfo())
	}

	@Test
	fun shouldReadWriteRAMView() {
		readWrite(RAMView(), RAMViewBeanInfo())
	}

	// net

	@Test
	fun shouldReadWriteConcentratorView() {
		readWrite(ConcentratorView(), ConcentratorViewBeanInfo())
	}

	@Test
	fun shouldReadWriteConstantView() {
		readWrite(ConstantView(), ConstantViewBeanInfo())
	}

	@Test
	fun shouldReadWriteDigitalEdgeView() {
		val graphView = GraphViewImpl()
		val component = DigitalEdgeView()
		graphView.add(component)
		readWrite(component, DigitalEdgeViewBeanInfo())
	}

	@Test
	fun shouldReadWriteProbeView() {
		readWrite(ProbeView(), ProbeViewBeanInfo())
	}

	@Test
	fun shouldReadWriteSplitterView() {
		readWrite(SplitterView(), SplitterViewBeanInfo())
	}

	@Test
	fun shouldReadWriteTunnelView() {
		readWrite(TunnelView(), TunnelViewBeanInfo())
	}

	@Test
	fun shouldReadWriteBreakView() {
		readWrite(BreakView(), BreakViewBeanInfo())
	}

	// output

	@Test
	fun shouldReadWriteLEDMatrixView() {
		readWrite(LEDMatrixView(), LEDMatrixViewBeanInfo())
	}

	@Test
	fun shouldReadWriteLEDView() {
		readWrite(LEDView(), LEDViewBeanInfo())
	}

	@Test
	fun shouldReadWriteRgbLEDView() {
		readWrite(RgbLEDView(), RgbLEDViewBeanInfo())
	}

	@Test
	fun shouldReadWriteSevenSegmentDisplayView() {
		readWrite(SevenSegmentDisplayView(), SevenSegmentDisplayViewBeanInfo())
	}

	@Test
	fun shouldReadWriteTerminalView() {
		readWrite(TerminalView(), TerminalViewBeanInfo())
	}
}