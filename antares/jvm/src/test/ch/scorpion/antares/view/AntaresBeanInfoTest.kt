package ch.scorpion.antares.view

import ch.scorpion.antares.AntaresTestRule
import ch.scorpion.antares.view.arithmetic.RandomView
import ch.scorpion.antares.view.arithmetic.RandomViewBeanInfo
import ch.scorpion.antares.view.container.DigitalPortViewComponent
import ch.scorpion.antares.view.container.DigitalPortViewComponentBeanInfo
import ch.scorpion.antares.view.gate.*
import ch.scorpion.antares.view.inout.CircuitInOutView
import ch.scorpion.antares.view.inout.CircuitInOutViewBeanInfo
import ch.scorpion.antares.view.input.*
import ch.scorpion.antares.view.addressable.RAMView
import ch.scorpion.antares.view.addressable.RAMViewBeanInfo
import ch.scorpion.antares.view.addressable.ROMView
import ch.scorpion.antares.view.addressable.ROMViewBeanInfo
import ch.scorpion.antares.view.net.*
import ch.scorpion.antares.view.output.*
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.edit.*
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Test

class AntaresBeanInfoTest {

	companion object {
		init {
			BaseModuleJvm.require()
			AntaresTestRule.configure()
		}
	}

	private val drawing = mockk<Drawing<Component>>(relaxed = true)
	private val commandManager = mockk<CommandManager>(relaxed = true)
	private val editor = mockk<Editor>(relaxed = true)

	init {
		every { editor.active } returns true
		every { editor.drawing } returns drawing
		every { editor.commandManager } returns commandManager

		val command = slot<Command>()
		every { commandManager.beginTransaction(capture(command)) } answers {
			command.captured.execute()
			Unit
		}
	}

	private fun <T: Component> readWrite(component: T, beanInfo: AbstractBeanInfo<T>) {
		every { drawing.getWithId(any()) } returns component
		beanInfo
			.getProperties(component, editor)
			.forEach { it.readFromObject(component) }

		beanInfo
			.getProperties(component, editor)
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

	// container

	@Test
	fun shouldReadWriteDigitalPortViewComponent() {
		readWrite(DigitalPortViewComponent(portView = DigitalPortView()), DigitalPortViewComponentBeanInfo())
	}

	// gate

	@Test
	fun shouldReadWriteAndGateView() {
		readWrite(AndGateView(), AndGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteBufferGateView() {
		readWrite(BufferGateView(), BufferGateViewBeanInfo())
	}

	@Test
	fun shouldDelayGateView() {
		readWrite(DelayGateView(), DelayGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteNandGateView() {
		readWrite(NandGateView(), NandGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteNorGateView() {
		readWrite(NorGateView(), NorGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteNotGateView() {
		readWrite(NotGateView(), NotGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteOrGateView() {
		readWrite(OrGateView(), OrGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteTriStateBufferGateView() {
		readWrite(TriStateBufferGateView(), TriStateBufferGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteXnorGateView() {
		readWrite(XnorGateView(), XnorGateViewBeanInfo())
	}

	@Test
	fun shouldReadWriteXorGateView() {
		readWrite(XorGateView(), XorGateViewBeanInfo())
	}

	// inout

	@Test
	fun shouldReadWriteCircuitInOutView() {
		readWrite(CircuitInOutView(), CircuitInOutViewBeanInfo())
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