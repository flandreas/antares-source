package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.edit.model.ComponentMessage
import ch.scorpion.jabbah.edit.model.ComponentMessageType
import ch.scorpion.jabbah.io.Storable

/**
 * Abstract base implementation of the [Application] interface to be used for all target environments.
 */
abstract class AbstractApplication(
    protected val eventBus: EventBus
) : Application {

	companion object {
		private val LOG by lazy { logger(AbstractApplication::class) }
	}

    init {
        configureCustomModules()
	    eventBus.register(CloseApplicationDataRequest::class) { close() }
    }

    /** ---- [Application] interface */

    override var data: ApplicationData? = null
	    set(value) {
		    if (field !== value) {
			    val oldField = field
			    field = value
			    eventBus.post(ApplicationDataEvent(this, oldField, field))
			    eventBus.post(CurrentSavableEvent(this, field?.savable))
		    }
	    }


	override val aboutInfo: AboutInfo get() = AboutInfo(
		iconPath = null,
		name = displayName,
		claim = "A cool application",
		version = "1.0",
		disclaimer = "All rights reserved."
	)

    override fun newFile() {
        if (canReplaceSavable("file.action.new.name")) {
	        data = ApplicationData(createNewApplicationData(), createNewSavable())
        }
    }

    override fun open(data: ApplicationData) {
        if (canReplaceSavable("file.action.open.name")) {
            this.data = data
        }
    }

    override fun save() {
        if (data == null) {
            throw IllegalStateException("No Savable available")
        }
        data!!.savable.save(this)
	    eventBus.post(CurrentSavableEvent(this, data!!.savable))
	    eventBus.post(ComponentMessage(type = ComponentMessageType.Info, source = null, messageKey = "application.data.saved.msg"))
    }

    override fun close() {
	    if (canReplaceSavable("file.action.close.name")) {
		    data = null
	    }
    }

	override fun showAboutInfo() {
		LOG.value.info("${aboutInfo.name} Version ${aboutInfo.version}")
	}

    /** ---- [AbstractApplication] */

    /**
     * Decides whether the current [Savable] can be replaced by new data.
     *
     * Gives the user the chance to save any changed data before they are replaced.
     *
     * @param actionKey the translation key of the action that wants to replace the [Savable].
     * @return `true` if the calling method can replace the current [Savable] with new data, `true`
     *         if the current [Savable] cannot be replaced, which must abort the calling replacement process.
     */
    protected abstract fun canReplaceSavable(actionKey: String): Boolean

    protected abstract fun createNewApplicationData(): Storable

    protected abstract fun createNewSavable(): Savable

    /** Called from the constructor to allow subclasses to require custom modules.*/
    private fun configureCustomModules() {
        // empty
    }

    /** Initialisation method that is called after the [AbstractApplication] constructor has been left.*/
    protected open fun init() {
        // empty
    }
}