package ch.scorpion.antares.model.addressable

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.edit.auth.Authorizer
import ch.scorpion.jabbah.edit.auth.Operation
import ch.scorpion.jabbah.graph.library.AbstractLibraryItemSavable
import ch.scorpion.jabbah.io.Storable

class MemorySavable(
    item: MemoryLibraryItem,
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItemSavable(item) {

    private val memoryLibraryItem: MemoryLibraryItem get() = item as MemoryLibraryItem

    override val typeName: String get() = Translations.getString("library.element.memory.name")

    override val description: String
        get() = "${Translations.getString("project.savable.prefix")} \"${memoryLibraryItem.name.getTranslation()}\""

    override val editable: Boolean
        get() = item.library?.let { Authorizer.isCurrentUserAuthorizedTo(Operation.Change, it) } ?: false

    override fun open(application: Application): Boolean {
        eventBus.post(OpenMemoryLibraryItemRequest(memoryLibraryItem))
        return true
    }

    override fun save(appDataViewController: ApplicationDataViewController): Boolean {
        memoryLibraryItem.updateStorable((appDataViewController.data!!.content as MemoryLibraryItem).storable)
        with (item.library!!) {
            libraryService.updateLibraryItem(this, item)
        }
        appDataViewController.data = appDataViewController.data!!.withSavable(this)
        return true
    }

    override fun getPropertyBean(storable: Storable): Bean = (storable as MemoryLibraryItem).storable
}