package io.antarescircuit.jabbah.graph.view

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.draw.container.DrawableGeometryProperty
import io.antarescircuit.jabbah.draw.container.DrawableProperty

/**
 * A [DrawableProperty] delegate property implementation of [ControlViewSource] properties
 * that post a [ControlViewSourceEvent] after a value has changed.
 */
class ControlViewSourceProperty<T>(
	initialValue: T,
	private val eventBus: EventBus = BaseModule.eventBus,
	afterSet: (() -> Unit)? = null
) : DrawableProperty<ControlViewSource<*>, T>(
	initialValue,
	afterSet,
	{ it.postControlViewSourceChangeEvent(eventBus)}
)

/**
 * A [DrawableGeometryProperty] delegate property implementation of [ControlViewSource] properties
 * that post a [ControlViewSourceEvent] after a value has changed.
 */
class ControlViewSourceGeometryProperty<T>(
	initialValue: T,
	private val eventBus: EventBus = BaseModule.eventBus,
	afterSet: (() -> Unit)? = null
) : DrawableGeometryProperty<ControlViewSource<*>, T>(
	initialValue,
	afterSet,
	{ it.postControlViewSourceChangeEvent(eventBus)}
)
