package io.antarescircuit.jabbah.app.properties

import io.antarescircuit.jabbah.app.ApplicationData
import io.antarescircuit.jabbah.app.ApplicationDataEvent
import io.antarescircuit.jabbah.base.Properties
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.CurrentEditorEvent
import io.antarescircuit.jabbah.edit.Editor
import io.antarescircuit.jabbah.edit.properties.ComponentPropertyPanelController
import io.antarescircuit.jabbah.edit.properties.applicationDataBeanProvider

/**
 * Sets the current [ApplicationData]'s bean as [defaultBean].
 *
 * @param currentEditorEventFilter determines how this [ApplicationDataPropertyPanelController] should react to
 * [CurrentEditorEvent]s
 */
class ApplicationDataPropertyPanelController(
    editor: Editor,
    eventBus: EventBus = BaseModule.eventBus,
    currentEditorEventFilter: ((CurrentEditorEvent) -> Boolean)? = null,
    properties: Properties = BaseModule.properties
) : ComponentPropertyPanelController(editor, eventBus, currentEditorEventFilter, properties) {

    private val applicationDataHandler: EventHandler<ApplicationDataEvent> = { handle(it) }

    init {
        eventBus.register(ApplicationDataEvent::class, applicationDataHandler)
    }

    override fun dispose() {
        super.dispose()
        eventBus.unregister(applicationDataHandler)
    }

    override val defaultBean: Any? get() {
        val beans = applicationDataBeanProvider.invoke(editor, emptyList())
        return if (beans.size == 1) {
            beans.first()
        } else {
            super.defaultBean
        }
    }

    private fun handle(@Suppress("UNUSED_PARAMETER") event: ApplicationDataEvent) {
        bean = defaultBean
    }
}