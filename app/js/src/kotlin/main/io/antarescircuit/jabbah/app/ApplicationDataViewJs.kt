package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.io.Storable

/** A dummy implementation of [ApplicationDataView] on the JS platform.*/
class ApplicationDataViewJs : ApplicationDataView {

	override fun dispose() { }

	override fun decideSaveChangedData(action: String): SaveUnchangedDataDecision {
		// TODO
		return SaveUnchangedDataDecision.No
	}

	override fun defineSavableForStoring(storable: Storable, currentSavable: Savable?): Savable? {
		// TODO
		return null
	}

	override fun defineSavableForLoading(): Savable? {
		// TODO
		return null
	}

	override fun showModalMessage(type: ModalMessageType, title: String, message: String) {
		// TODO
	}

	override fun registerKeepAliveUsage() {
		// Not used on JS platform
	}
}