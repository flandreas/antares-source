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
import ch.scorpion.antares.view.memory.RAMView
import ch.scorpion.antares.view.memory.RAMViewBeanInfo
import ch.scorpion.antares.view.memory.ROMView
import ch.scorpion.antares.view.memory.ROMViewBeanInfo
import ch.scorpion.antares.view.net.*
import ch.scorpion.antares.view.output.*
import ch.scorpion.antares.view.port.DigitalPortView
import ch.scorpion.jabbah.base.module.BaseModuleJvm
import ch.scorpion.jabbah.edit.Component
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.AbstractBeanInfo
import ch.scorpion.jabbah.graph.view.graph.GraphViewImpl
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class AntaresBeanInfoTest {

	companion object {
		init {
			BaseModuleJvm.require()
			AntaresTestRule.configure()
		}
	}

	private val editor = mockk<Editor>()

	init {
		every { editor.active } returns true
	}

	private fun <T: Component> read(component: T, beanInfo: AbstractBeanInfo<T>) {
		beanInfo
			.getProperties(component, editor)
			.forEach { it.readFromObject(component) }
	}

	// arithmetic

	@Test
	fun shouldReadRandomView() {
		read(RandomView(), RandomViewBeanInfo())
	}

	// container

	@Test
	fun shouldReadDigitalPortViewComponent() {
		read(DigitalPortViewComponent(portView = DigitalPortView()), DigitalPortViewComponentBeanInfo())
	}

	// gate

	@Test
	fun shouldReadAndGateView() {
		read(AndGateView(), AndGateViewBeanInfo())
	}

	@Test
	fun shouldReadBufferGateView() {
		read(BufferGateView(), BufferGateViewBeanInfo())
	}

	@Test
	fun shouldDelayGateView() {
		read(DelayGateView(), DelayGateViewBeanInfo())
	}

	@Test
	fun shouldReadNandGateView() {
		read(NandGateView(), NandGateViewBeanInfo())
	}

	@Test
	fun shouldReadNorGateView() {
		read(NorGateView(), NorGateViewBeanInfo())
	}

	@Test
	fun shouldReadNotGateView() {
		read(NotGateView(), NotGateViewBeanInfo())
	}

	@Test
	fun shouldReadOrGateView() {
		read(OrGateView(), OrGateViewBeanInfo())
	}

	@Test
	fun shouldReadTriStateBufferGateView() {
		read(TriStateBufferGateView(), TriStateBufferGateViewBeanInfo())
	}

	@Test
	fun shouldReadXnorGateView() {
		read(XnorGateView(), XnorGateViewBeanInfo())
	}

	@Test
	fun shouldReadXorGateView() {
		read(XorGateView(), XorGateViewBeanInfo())
	}

	// inout

	@Test
	fun shouldReadCircuitInOutView() {
		read(CircuitInOutView(), CircuitInOutViewBeanInfo())
	}

	// input

	@Test
	fun shouldReadClockView() {
		read(ClockView(), ClockViewBeanInfo())
	}

	@Test
	fun shouldReadDipSwitchView() {
		read(DipSwitchView(), DipSwitchViewBeanInfo())
	}

	@Test
	fun shouldReadKeyboardView() {
		read(KeyboardView(), KeyboardViewBeanInfo())
	}

	@Test
	fun shouldReadSwitchView() {
		read(SwitchView(), SwitchViewBeanInfo())
	}

	// memory

	@Test
	fun shouldReadROMView() {
		read(ROMView(), ROMViewBeanInfo())
	}

	@Test
	fun shouldReadRAMView() {
		read(RAMView(), RAMViewBeanInfo())
	}

	// net

	@Test
	fun shouldReadConcentratorView() {
		read(ConcentratorView(), ConcentratorViewBeanInfo())
	}

	@Test
	fun shouldReadConstantView() {
		read(ConstantView(), ConstantViewBeanInfo())
	}

	@Test
	fun shouldReadDigitalEdgeView() {
		val graphView = GraphViewImpl()
		val component = DigitalEdgeView()
		graphView.add(component)
		read(component, DigitalEdgeViewBeanInfo())
	}

	@Test
	fun shouldReadProbeView() {
		read(ProbeView(), ProbeViewBeanInfo())
	}

	@Test
	fun shouldReadSplitterView() {
		read(SplitterView(), SplitterViewBeanInfo())
	}

	@Test
	fun shouldReadTunnelView() {
		read(TunnelView(), TunnelViewBeanInfo())
	}

	// output

	@Test
	fun shouldReadLEDMatrixView() {
		read(LEDMatrixView(), LEDMatrixViewBeanInfo())
	}

	@Test
	fun shouldReadLEDView() {
		read(LEDView(), LEDViewBeanInfo())
	}

	@Test
	fun shouldReadRgbLEDView() {
		read(RgbLEDView(), RgbLEDViewBeanInfo())
	}

	@Test
	fun shouldReadSevenSegmentDisplayView() {
		read(SevenSegmentDisplayView(), SevenSegmentDisplayViewBeanInfo())
	}

	@Test
	fun shouldReadTerminalView() {
		read(TerminalView(), TerminalViewBeanInfo())
	}
}