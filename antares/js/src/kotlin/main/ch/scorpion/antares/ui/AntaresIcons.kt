package ch.scorpion.antares.ui

import ch.scorpion.jabbah.base.mreact.IconProviderRegistry
import react.RBuilder
import react.ReactElement
import react.buildElement
import react.dom.img

private const val size = "28"

fun RBuilder.sevenSegment(): ReactElement = buildElement { img(src="img/7segment.svg") { attrs.width = size } }
fun RBuilder.andGate(): ReactElement = buildElement { img(src="img/and.svg") { attrs.width = size } }
fun RBuilder.breakComponent(): ReactElement = buildElement { img(src="img/break.svg") { attrs.width = size } }
fun RBuilder.buffer(): ReactElement = buildElement { img(src="img/buffer.svg") { attrs.width = size } }
fun RBuilder.clock(): ReactElement = buildElement { img(src="img/clock.svg") { attrs.width = size } }
fun RBuilder.concentrator(): ReactElement = buildElement { img(src="img/concentrator.svg") { attrs.width = size } }
fun RBuilder.constant(): ReactElement = buildElement { img(src="img/constant.svg") { attrs.width = size } }
fun RBuilder.delay(): ReactElement = buildElement { img(src="img/delay.svg") { attrs.width = size } }
fun RBuilder.dipSwitch(): ReactElement = buildElement { img(src="img/dip-switch.svg") { attrs.width = size } }
fun RBuilder.inout(): ReactElement = buildElement { img(src="img/inout.svg") { attrs.width = size } }
fun RBuilder.input(): ReactElement = buildElement { img(src="img/input.svg") { attrs.width = size } }
fun RBuilder.keyboard(): ReactElement = buildElement { img(src="img/keyboard.svg") { attrs.width = size } }
fun RBuilder.ledMatrix(): ReactElement = buildElement { img(src="img/led-matrix.svg") { attrs.width = size } }
fun RBuilder.led(): ReactElement = buildElement { img(src="img/led.svg") { attrs.width = size } }
fun RBuilder.nandGate(): ReactElement = buildElement { img(src="img/nand.svg") { attrs.width = size } }
fun RBuilder.norGate(): ReactElement = buildElement { img(src="img/nor.svg") { attrs.width = size } }
fun RBuilder.notGate(): ReactElement = buildElement { img(src="img/not.svg") { attrs.width = size } }
fun RBuilder.orGate(): ReactElement = buildElement { img(src="img/or.svg") { attrs.width = size } }
fun RBuilder.output(): ReactElement = buildElement { img(src="img/output.svg") { attrs.width = size } }
fun RBuilder.probe(): ReactElement = buildElement { img(src="img/probe.svg") { attrs.width = size } }
fun RBuilder.ram(): ReactElement = buildElement { img(src="img/ram.svg") { attrs.width = size } }
fun RBuilder.random(): ReactElement = buildElement { img(src="img/random.svg") { attrs.width = size } }
fun RBuilder.rgbLed(): ReactElement = buildElement { img(src="img/rgb-led.svg") { attrs.width = size } }
fun RBuilder.rom(): ReactElement = buildElement { img(src="img/rom.svg") { attrs.width = size } }
fun RBuilder.splitter(): ReactElement = buildElement { img(src="img/splitter.svg") { attrs.width = size } }
fun RBuilder.switch(): ReactElement = buildElement { img(src="img/switch.svg") { attrs.width = size } }
fun RBuilder.terminal(): ReactElement = buildElement { img(src="img/terminal.svg") { attrs.width = size } }
fun RBuilder.tristateBuffer(): ReactElement = buildElement { img(src="img/tristate-buffer.svg") { attrs.width = size } }
fun RBuilder.tunnel(): ReactElement = buildElement { img(src="img/tunnel.svg") { attrs.width = size } }
fun RBuilder.xnorGate(): ReactElement = buildElement { img(src="img/xnor.svg") { attrs.width = size } }
fun RBuilder.xorGate(): ReactElement = buildElement { img(src="img/xor.svg") { attrs.width = size } }
fun RBuilder.pullResistor(): ReactElement = buildElement { img(src="img/pull-resistor.svg") { attrs.width = size } }
fun RBuilder.transistor(): ReactElement = buildElement { img(src="img/transistor.svg") { attrs.width = size } }
fun RBuilder.ground(): ReactElement = buildElement { img(src="img/ground.svg") { attrs.width = size } }
fun RBuilder.power(): ReactElement = buildElement { img(src="img/power.svg") { attrs.width = size } }
fun RBuilder.realSwitch(): ReactElement = buildElement { img(src="img/real-switch.svg") { attrs.width = size } }
fun RBuilder.joystick(): ReactElement = buildElement { img(src="img/joystick.svg") { attrs.width = size } }

fun registerAntaresIconsInProvider() {
	IconProviderRegistry.register("/img/7segment.png") { RBuilder().sevenSegment() }
	IconProviderRegistry.register("/img/and.png") { RBuilder().andGate() }
	IconProviderRegistry.register("/img/break.png") { RBuilder().breakComponent() }
	IconProviderRegistry.register("/img/buffer.png") { RBuilder().buffer() }
	IconProviderRegistry.register("/img/clock.png") { RBuilder().clock() }
	IconProviderRegistry.register("/img/concentrator.png") { RBuilder().concentrator() }
	IconProviderRegistry.register("/img/constant.png") { RBuilder().constant() }
	IconProviderRegistry.register("/img/delay.png") { RBuilder().delay() }
	IconProviderRegistry.register("/img/dip-switch.png") { RBuilder().dipSwitch() }
	IconProviderRegistry.register("/img/inout.png") { RBuilder().inout() }
	IconProviderRegistry.register("/img/input.png") { RBuilder().input() }
	IconProviderRegistry.register("/img/keyboard.png") { RBuilder().keyboard() }
	IconProviderRegistry.register("/img/led-matrix.png") { RBuilder().ledMatrix() }
	IconProviderRegistry.register("/img/led.png") { RBuilder().led() }
	IconProviderRegistry.register("/img/nand.png") { RBuilder().nandGate() }
	IconProviderRegistry.register("/img/nor.png") { RBuilder().norGate() }
	IconProviderRegistry.register("/img/not.png") { RBuilder().notGate() }
	IconProviderRegistry.register("/img/or.png") { RBuilder().orGate() }
	IconProviderRegistry.register("/img/output.png") { RBuilder().output() }
	IconProviderRegistry.register("/img/probe.png") { RBuilder().probe() }
	IconProviderRegistry.register("/img/ram.png") { RBuilder().ram() }
	IconProviderRegistry.register("/img/random.png") { RBuilder().random() }
	IconProviderRegistry.register("/img/rgb-led.png") { RBuilder().rgbLed() }
	IconProviderRegistry.register("/img/rom.png") { RBuilder().rom() }
	IconProviderRegistry.register("/img/splitter.png") { RBuilder().splitter() }
	IconProviderRegistry.register("/img/terminal.png") { RBuilder().terminal() }
	IconProviderRegistry.register("/img/switch.png") { RBuilder().switch() }
	IconProviderRegistry.register("/img/tristate-buffer.png") { RBuilder().tristateBuffer() }
	IconProviderRegistry.register("/img/tunnel.png") { RBuilder().tunnel() }
	IconProviderRegistry.register("/img/xnor.png") { RBuilder().xnorGate() }
	IconProviderRegistry.register("/img/xor.png") { RBuilder().xorGate() }
	IconProviderRegistry.register("/img/pull-resistor.png") { RBuilder().pullResistor() }
	IconProviderRegistry.register("/img/transistor.png") { RBuilder().transistor() }
	IconProviderRegistry.register("/img/ground.png") { RBuilder().ground() }
	IconProviderRegistry.register("/img/power.png") { RBuilder().power() }
	IconProviderRegistry.register("/img/real-switch.png") { RBuilder().realSwitch() }
	IconProviderRegistry.register("/img/joystick.png") { RBuilder().joystick() }
}
