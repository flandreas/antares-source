package ch.scorpion.antares.model.analog

import ch.scorpion.jabbah.graph.model.Net
import ch.scorpion.jabbah.graph.model.net.NetImpl

class AnalogNet : NetImpl<AnalogSignal>() {

	override fun cloneEmpty(): Net<AnalogSignal> = AnalogNet()
}