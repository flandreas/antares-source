package io.antarescircuit.antares.model.fsm

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ApplicationDataViewController
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.graph.library.AbstractLibraryItemSavable
import io.antarescircuit.jabbah.io.Storable

class FSMSavable(
    item: FSMLibraryItem,
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItemSavable(item) {

    override val typeName: String get() = Translations.getString("library.element.fsm.name")

    private val fsmLibraryItem: FSMLibraryItem get() = item as FSMLibraryItem

    override fun open(application: Application): Boolean {
        eventBus.post(OpenFSMLibraryItemRequest(fsmLibraryItem))
        return true
    }

    override fun save(appDataViewController: ApplicationDataViewController): Boolean {
        fsmLibraryItem.updateStorable(appDataViewController.data!!.content as FSMDrawing)
        with (item.library!!) {
            libraryService.updateLibraryItem(this, item)
        }
        appDataViewController.data = appDataViewController.data!!.withSavable(this)
        return true
    }

    override fun getPropertyBean(storable: Storable): Bean = storable as FSMDrawing
}