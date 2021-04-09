package ch.scorpion.jabbah.graph

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.graph.model.GraphPort
import ch.scorpion.jabbah.graph.model.GraphPortTypeChanged
import ch.scorpion.jabbah.graph.model.PortType
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot

class GraphPortMockBuilder<T: Any>(
	private val eventBus: EventBus = BaseModule.eventBus
) {

	val graphPort = mockk<GraphPort<T>>(relaxed = true)
	private val portTypeSlot = slot<PortType>()
	private lateinit var portType: PortType

	init {
		withPortType(PortType.INPUT)
		every { graphPort.portType = capture(portTypeSlot) } answers {
			eventBus.post(GraphPortTypeChanged(graphPort, portType, portTypeSlot.captured))
			Unit
		}

	}

	fun withPortType(newPortType: PortType): GraphPortMockBuilder<T> {
		every { graphPort.portType } answers {
			portType = newPortType
			newPortType
		}
		return this
	}

	fun build(): GraphPort<T> = graphPort
}