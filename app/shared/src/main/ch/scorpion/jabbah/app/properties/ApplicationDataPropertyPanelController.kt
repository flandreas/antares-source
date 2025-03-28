package ch.scorpion.jabbah.app.properties

import ch.scorpion.jabbah.app.ApplicationData
import ch.scorpion.jabbah.app.ApplicationDataEvent
import ch.scorpion.jabbah.base.Properties
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.event.EventHandler
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CurrentEditorEvent
import ch.scorpion.jabbah.edit.Editor
import ch.scorpion.jabbah.edit.properties.ComponentPropertyPanelController
import ch.scorpion.jabbah.edit.properties.applicationDataBeanProvider

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