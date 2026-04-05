package io.antarescircuit.jabbah.graph.model

import io.antarescircuit.jabbah.base.event.EventBus

/** Posted on [EventBus] by certain [Vertice]s to indicate a signal value to be logged.*/
data class LogEvent(
	val source: Vertice,
	val name: String,
	val value: String,
	val time: Long
)