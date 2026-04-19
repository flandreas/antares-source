package io.antarescircuit.jabbah.app

import io.antarescircuit.jabbah.app.SaveUnchangedDataDecision.*
import io.antarescircuit.jabbah.app.action.SaveFileAction
import io.antarescircuit.jabbah.edit.properties.applicationDataBeanProvider
import io.antarescircuit.jabbah.base.Translations
import io.antarescircuit.jabbah.base.event.EventBus
import io.antarescircuit.jabbah.base.event.EventHandler
import io.antarescircuit.jabbah.base.event.PropertyOwner
import io.antarescircuit.jabbah.base.event.PropertyOwnerImpl
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.base.module.BaseModule
import io.antarescircuit.jabbah.base.ui.AbstractUIController
import io.antarescircuit.jabbah.base.ui.UIView
import io.antarescircuit.jabbah.draw.ui.Toast
import io.antarescircuit.jabbah.edit.BeanProvider
import io.antarescircuit.jabbah.edit.CommandManager
import io.antarescircuit.jabbah.edit.UndoableDataHolder
import io.antarescircuit.jabbah.edit.module.EditModule
import io.antarescircuit.jabbah.io.Storable

enum class SaveUnchangedDataDecision {
	Yes,
	No,
	Cancel
}

enum class ModalMessageType {
	Info,
	Warning,
	Error
}

/**
 * Defines operations of the UI of an [Application] that is required for loading and
 * storing [ApplicationData].
 */
interface ApplicationDataView : UIView {

	fun decideSaveChangedData(action: String): SaveUnchangedDataDecision

	fun defineSavableForStoring(storable: Storable, currentSavable: Savable?): Savable?

	fun defineSavableForLoading(): Savable?

	fun showModalMessage(type: ModalMessageType, title: String, message: String)

	/**
	 * Register application usage if supported and required by the current platform.
	 * Called if [ApplicationData] is opened. Tracks usage if previous registration is
	 * older than a certain time. Used to track usage even if users seldom restart the application.
	 *
	 * Part of the [UIView] because the controller is platform-agnostic.
	 */
	fun registerKeepAliveUsage()
}

/**
 * Contains the part of the [Application] logic that maintains the [ApplicationData] and provides
 * operations for loading and storing.
 *
 * Sends [ApplicationDataEvent]s and [CurrentSavableEvent]s when the [ApplicationData] gets set.
 */
open class ApplicationDataViewController(
	private val commandManager: CommandManager = EditModule.commandManager,
	private val newStorableProvider: () -> Storable,
	private val repository: ApplicationDataRepository<Savable>,
	val eventBus: EventBus = BaseModule.eventBus,
	propertyOwnerImpl: PropertyOwner<Any> = PropertyOwnerImpl()
) : AbstractUIController<ApplicationDataView>(), ApplicationDataHolder, PropertyOwner<Any> by propertyOwnerImpl {

	companion object {
		private val LOG by logger(ApplicationDataViewController::class)
		const val PROP_SAVABLE = "appDataView.savable"
	}

	private val closeAppDataRequestHandler: EventHandler<CloseApplicationDataRequest> = {
		data = null
	}

	override fun dispose() {
		super.dispose()
		saveAction.dispose()
		eventBus.unregister(closeAppDataRequestHandler)
	}

	val saveAction by lazy { SaveFileAction(this, eventBus, commandManager) }

	private val applicationDataViewBeanProvider : BeanProvider = { _, _ ->
		if (data?.savable != null) {
			val bean = data!!.savable.getPropertyBean(data!!.content)
			if (bean != null) {
				listOf(bean)
			} else {
				emptyList()
			}
		} else {
			emptyList()
		}
	}

	/**
	 * Determines whether saving is currently possible.
	 * Can be controlled by higher-level classes that e.g. implement special application modes like
	 * "Simulation", in which saving is temporarily disabled, until the mode returns to "Editing", where
	 * saving is enabled.
	 */
	var isSavable: Boolean = true
		set(value) {
			if (field != value) {
				field = value
				fire(PROP_SAVABLE, !field, field)
			}
		}

	init {
		source = this
		commandManager.bindDataHolder(this)
		applicationDataBeanProvider = this.applicationDataViewBeanProvider
		eventBus.register(CloseApplicationDataRequest::class, closeAppDataRequestHandler)
	}

	/** ---- [UndoableDataHolder] interface */

	override fun getUndoableState(): Storable? = data?.content

	override fun setUndoableState(state: Storable) {
		data!!.content = state
	}

	override fun undoableStateEstablished(state: Storable) {
		data?.let {
			eventBus.post(ApplicationDataContentEstablishedEvent(it))
		}
	}

	/** ---- [ApplicationDataHolder] interface */

	override var data: ApplicationData? = null
		set(value) {
			val oldField = field
			field = value
			commandManager.reset()
			eventBus.post(ApplicationDataEvent(oldField, field))
			eventBus.post(CurrentSavableEvent(field?.savable))

			// Don't dispose oldField. It is unclear how current application keep cached version
			// of the former content, which would be destroyed by disposing it.

			if (value != null && value.savable.supportsMostRecent && value.savable.defined) {
				mostRecentSavables.register(value.savable)
			}
		}
	/** ---- [ApplicationDataViewController] */

	val mostRecentSavables = SavableHistory(eventBus = eventBus)

	/**
	 * Creates a new, empty [ApplicationData] object and sets it as the current [data].
	 *
	 * If the old current [ApplicationData] has been changed, the user is asked if he wants to
	 * save the old data first, or if he wants to cancel the operation.
	 */
	fun newData() {
		if (canReplaceSavable("file.action.new.name")) {
			LOG.userTrail("Set new empty application data")
			data = ApplicationData(newStorableProvider.invoke(), repository.createUndefinedSavable())
		}
	}

	/**
	 * Registers the specified [ApplicationData] as the current one.
	 *
	 * If the old current [ApplicationData] has been changed, the user is asked if he wants to
	 * save the old data first, or if he wants to cancel the operation.
	 */
	fun open(provider: () -> ApplicationData) {
		if (canReplaceSavable("file.action.open.name")) {
			this.data = provider.invoke()
		}
	}

	/**
	 * Asks the user to define a [Savable] to open a [Storable] from the [ApplicationDataRepository]
	 * and sets it as the new current [ApplicationData].
	 *
	 * If the old current [ApplicationData] has been changed, the user is asked if he wants to
	 * save the old data first, or if he wants to cancel the operation.
	 *
	 * If the user cancels the open operation, nothing is changed.
	 */
	fun open() {
		if (canReplaceSavable("file.action.open.name")) {
			view.defineSavableForLoading()?.let {
				LOG.userTrail("Open application data from ${it.description}")
				val storable = repository.load(it)
				data = ApplicationData(storable, it)
			}
		}
	}

	open fun open(savable: Savable) {
		if (canReplaceSavable("file.action.open.name")) {
			LOG.userTrail("Open application data from ${savable.description}")
			val storable = repository.load(savable)
			data = ApplicationData(storable, savable)
		}
	}

	/**
	 * Closes the current [ApplicationData] without setting any new [ApplicationData].
	 *
	 * If the old current [ApplicationData] has been changed, the user is asked if he wants to
	 * save the old data first, or if he wants to cancel the operation.
	 */
	fun closeData() {
		if (canReplaceSavable("file.action.close.name")) {
			closeDataAfterConfirmation()
		}
	}

	protected fun closeDataAfterConfirmation() {
		if (data != null) {
			LOG.userTrail("Close application data")
			data = null
		}
	}

	/**
	 * Saves the current [ApplicationData] at the location indicated by its [Savable].
	 *
	 * This method is called by application components like a save action and dispatches
	 * to [Savable.save] in order to give concrete [Savable]s a chance to implement varying
	 * save strategies, such as calling particular services for saving instead of using
	 * this [ApplicationDataViewController]'s [ApplicationDataRepository]. Default implementations
	 * of [Savable] however will call the [ApplicationDataRepository] to use the application's
	 * default persistence mechanism, and call [saveWithSavable] in return.
	 */
	fun save() {
		data?.let {
			LOG.info("Save application data")
			if (it.savable.save(this)) {
				// Resetting CommandManager not necessary, already done in setData as a consequence of Savable.save
				eventBus.post(CurrentSavableEvent(it.savable))
				Toast.show(Translations.getString("application.data.saved.msg", it.savable.typeName))
			}
		} ?: throw IllegalStateException("Request to save without present data")
	}

	/**
	 * Saves the current [ApplicationData] at the location indicated by its [Savable]
	 * using this [ApplicationDataViewController]'s [ApplicationDataRepository].
	 *
	 * In contrast to [save], which first involves [Savable.save], this method directly
	 * calls [ApplicationDataRepository].
	 */
	fun saveWithSavable() {
		data?.let {
			if (it.savable.notDefined) {
				throw IllegalStateException("Savable is not defined")
			}
			LOG.info("Save application data")
			repository.store(it.savable, it.content)
		} ?: throw IllegalStateException("Request to save without present data")
	}

	/**
	 * Asks the user to define a [Savable] and uses it to save the current [ApplicationData] according
	 * to that [Savable].
	 * @return `false` if the user cancelled the save operation while defining the [Savable]
	 */
	fun saveAs(): Boolean {
		return view.defineSavableForStoring(data!!.content, data!!.savable)?.let { newSavable ->
			LOG.userTrail("Store application data")
			repository.store(newSavable, data!!.content)
			data = data!!.withSavable(newSavable)
			true
		} ?: false
	}

	/**
	 * Decides whether the current [Savable] can be replaced by new data.
	 *
	 * Gives the user the chance to save any changed data before they are replaced.
	 *
	 * @param actionKey the translation key of the action that wants to replace the [Savable].
	 * @return `true` if the calling method can replace the current [Savable] with new data, `true`
	 *         if the current [Savable] cannot be replaced, which must abort the calling replacement process.
	 */
	fun canReplaceSavable(actionKey: String): Boolean {
		if (!commandManager.canUndo()) {
			return true
		}
		return when(view.decideSaveChangedData(actionKey)) {
			No -> {
				commandManager.reset()
				true
			}
			Cancel -> false
			Yes -> data?.savable?.save(this) ?: true
		}
	}
}