package ch.scorpion.antares.view.analog

import ch.scorpion.antares.model.analog.AnalogNet
import ch.scorpion.antares.model.analog.AnalogSignal
import ch.scorpion.jabbah.draw.style.DrawStyleModule
import ch.scorpion.jabbah.draw.style.StyleProvider
import ch.scorpion.jabbah.graph.view.net.edge.EdgeViewImpl

class AnalogEdgeView(
	styleProvider: StyleProvider = DrawStyleModule.styleProvider,
	net: AnalogNet = AnalogNet()
) : EdgeViewImpl<AnalogSignal>(
	styleProvider,
	DEF_EDGE_TO_PORT_CONNECTOR_SUPPLIER,
	DEF_ORIG_ENDPOINT_CONNECTOR_SUPPLIER,
	DEF_DEST_ENDPOINT_CONNECTOR_SUPPLIER,
	net
)