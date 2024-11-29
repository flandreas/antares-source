package ch.scorpion.antares.view.addressable

import ch.scorpion.antares.model.addressable.Addressable
import ch.scorpion.antares.model.addressable.MemoryLibraryItem
import ch.scorpion.jabbah.app.ApplicationDataHolder
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.CommandManager
import ch.scorpion.jabbah.graph.AbstractTitledGraphDesktopViewItemSwing
import ch.scorpion.jabbah.graph.GraphApplicationContextHolder
import ch.scorpion.jabbah.graph.model.Graph
import ch.scorpion.jabbah.graph.model.vertice.ObjectLink

class MemoryStorableGraphDesktopItemSwing(
    item: MemoryLibraryItem,
    private val applicationDataHolder: ApplicationDataHolder,
    applicationContextHolder: GraphApplicationContextHolder,
    commandManager: CommandManager,
    eventBus: EventBus = BaseModule.eventBus
) : AbstractTitledGraphDesktopViewItemSwing(
        createTitleText(item.memoryStorable),
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

    private class MemoryStorableLink(
        private val applicationDataHolder: ApplicationDataHolder
    ) : ObjectLink<Addressable> {

        override fun getLinkedObject(startGraph: Graph?): Addressable {
            return applicationDataHolder.data!!.content as MemoryStorable
        }
    }
}