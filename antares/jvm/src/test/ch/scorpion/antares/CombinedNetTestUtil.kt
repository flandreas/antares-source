package ch.scorpion.antares

import ch.scorpion.antares.model.net.DigitalCombinedNetAccess
import ch.scorpion.antares.model.signal.BitWidth
import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.jabbah.graph.model.OutputPort
import ch.scorpion.jabbah.graph.model.net.CombinedNet
import kotlin.test.assertEquals

fun checkCombinedNetAccess(
	combinedNet: CombinedNet<DigitalSignal>,
	port1: OutputPort<DigitalSignal>,
	width1: BitWidth,
	index1: Int,
	port2: OutputPort<DigitalSignal>,
	width2: BitWidth,
	index2: Int,
) {
	assertEquals(width1, (combinedNet.accessOf(port1) as DigitalCombinedNetAccess).width)
	assertEquals(index1, (combinedNet.accessOf(port1) as DigitalCombinedNetAccess).index)

	assertEquals(width2, (combinedNet.accessOf(port2) as DigitalCombinedNetAccess).width)
	assertEquals(index2, (combinedNet.accessOf(port2) as DigitalCombinedNetAccess).index)
}