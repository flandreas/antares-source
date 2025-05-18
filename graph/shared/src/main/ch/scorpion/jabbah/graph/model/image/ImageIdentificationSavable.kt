package ch.scorpion.jabbah.graph.model.image

import ch.scorpion.jabbah.app.Application
import ch.scorpion.jabbah.app.ApplicationDataViewController
import ch.scorpion.jabbah.base.Translations
import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.module.BaseModule
import ch.scorpion.jabbah.base.Bean
import ch.scorpion.jabbah.edit.model.image.ImageIdentification
import ch.scorpion.jabbah.graph.library.AbstractLibraryItemSavable
import ch.scorpion.jabbah.io.Storable

class ImageIdentificationSavable(
    element: ImageLibraryElement,
    private val eventBus: EventBus = BaseModule.eventBus
) : AbstractLibraryItemSavable(element) {

    private val imageLibraryElement: ImageLibraryElement get() = item as ImageLibraryElement

    override val typeName: String get() = Translations.getString("edit.component.image")

    override fun open(application: Application): Boolean {
        eventBus.post(OpenImageLibraryElementRequest(imageLibraryElement))
        return true
    }

    override fun save(appDataViewController: ApplicationDataViewController): Boolean {
        imageLibraryElement.updateStorable(appDataViewController.data!!.content as ImageIdentification)
        with (item.library!!) {
            libraryService.updateLibraryItem(this, item)
        }
        appDataViewController.data = appDataViewController.data!!.withSavable(this)
        return true
    }

    override fun getPropertyBean(storable: Storable): Bean = storable as ImageIdentification
}