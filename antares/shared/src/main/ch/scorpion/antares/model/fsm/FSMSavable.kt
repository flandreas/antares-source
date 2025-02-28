package ch.scorpion.antares.model.fsm

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.edit.Bean
import ch.scorpion.jabbah.graph.library.AbstractLibraryItemSavable
import ch.scorpion.jabbah.io.Storable

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