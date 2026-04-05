package io.antarescircuit.antares.view.addressable

import io.antarescircuit.antares.model.addressable.Addressable
import io.antarescircuit.antares.model.addressable.MemoryLibraryItem
import io.antarescircuit.antares.model.addressable.MemoryStorable
import io.antarescircuit.jabbah.app.ApplicationDataHolder
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.graph.AbstractTitledGraphDesktopViewItemSwing
import io.antarescircuit.jabbah.graph.GraphApplicationContextHolder
import io.antarescircuit.jabbah.graph.model.Graph
import io.antarescircuit.jabbah.graph.model.vertice.ObjectLink

class MemoryStorableGraphDesktopItemSwing(
    val item: MemoryLibraryItem,
    private val applicationDataHolder: ApplicationDataHolder,
    applicationContextHolder: GraphApplicationContextHolder,
    commandManager: CommandManager,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractTitledGraphDesktopViewItemSwing(
        createTitleText(item.storable),
        AddressableContentsPanel(
            null,
            applicationContextHolder,
            MemoryStorableLink(applicationDataHolder),
            commandManager
        ),
        applicationDataHolder,
        eventBus
    )
{
    companion object {
        fun createTitleText(storable: MemoryStorable): String =
            "${Translations.getString("library.element.memory.name")} \"${storable.name.getTranslation()}\""
    }

    private val memoryStorable: MemoryStorable get() = applicationDataHolder.data!!.content as MemoryStorable

    override fun createHeaderText(): String = createTitleText(memoryStorable)

    override fun displays(content: Any?): Boolean =
        applicationDataHolder.data?.content is MemoryStorable && content === memoryStorable

    private class MemoryStorableLink(
        private val applicationDataHolder: ApplicationDataHolder
    ) : ObjectLink<Addressable> {

        override fun getLinkedObject(startGraph: Graph?): Addressable =
            applicationDataHolder.data!!.content as MemoryStorable
    }
}