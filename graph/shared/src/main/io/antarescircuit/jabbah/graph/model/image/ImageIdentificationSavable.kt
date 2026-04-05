package io.antarescircuit.jabbah.graph.model.image

import io.antarescircuit.jabbah.app.Application
import io.antarescircuit.jabbah.app.ApplicationDataViewController
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.Bean
import io.antarescircuit.jabbah.edit.model.image.ImageIdentification
import io.antarescircuit.jabbah.graph.library.AbstractLibraryItemSavable
import io.antarescircuit.jabbah.io.Storable

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