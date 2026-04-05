package io.antarescircuit.antares.model.addressable

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ApplicationDataViewController
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.graph.library.AbstractLibraryItemSavable
import io.antarescircuit.jabbah.io.Storable

class MemorySavable(
    item: MemoryLibraryItem,
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItemSavable(item) {

    private val memoryLibraryItem: MemoryLibraryItem get() = item as MemoryLibraryItem

    override val typeName: String get() = Translations.getString("library.element.memory.name")

    override fun open(application: Application): Boolean {
        eventBus.post(OpenMemoryLibraryItemRequest(memoryLibraryItem))
        return true
    }

    override fun save(appDataViewController: ApplicationDataViewController): Boolean {
        memoryLibraryItem.updateStorable(appDataViewController.data!!.content as MemoryStorable)
        with (item.library!!) {
            libraryService.updateLibraryItem(this, item)
        }
        appDataViewController.data = appDataViewController.data!!.withSavable(this)
        return true
    }

    override fun getPropertyBean(storable: Storable): Bean = storable as MemoryStorable
}