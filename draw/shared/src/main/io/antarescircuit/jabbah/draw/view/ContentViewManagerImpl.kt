package io.antarescircuit.jabbah.draw.view

import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule

/**
 * Standard implementation of the [ContentViewManager] interface.
 */
class ContentViewManagerImpl(val eventBus: EventBus) : ContentViewManager {

    @Suppress("unused")
    constructor(): this(BaseModule.eventBus)

    override var activeView: ContentView<*>? = null
        set(value) {
            val oldView = field
            field = value
            eventBus.post(ActiveContentViewChangedEvent(this, oldView, field))
        }
}