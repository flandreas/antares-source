package ch.scorpion.jabbah.app

import ch.scorpion.jabbah.base.event.EventBus
import ch.scorpion.jabbah.base.exception.IllegalStateException
import ch.scorpion.jabbah.io.Storable

/**
 * Abstract base implementation of the [Application] interface to be used for all target environments.
 */
abstract class AbstractApplication(
    protected val eventBus: EventBus
) : Application {

    init {
        configureCustomModules()
    }

    /** ---- [Application] interface */

    override var applicationData: Storable? = null
    set(value) {
        field = value
        eventBus.post(ApplicationDataEvent(this, field))
    }

    override var savable: Savable? = null
        set(value) {
            field = value
            eventBus.post(CurrentSavableEvent(this, field))
        }

    override fun newFile() {
        if (canReplaceSavable("file.action.new.name")) {
            applicationData = createNewApplicationData()
            savable = createNewSavable()
        }
    }

    override fun open(storable: Storable, savable: Savable) {
        if (canReplaceSavable("file.action.open.name")) {
            applicationData = storable
            this.savable = savable
        }
    }

    override fun save() {
        if (savable == null) {
            throw IllegalStateException("No Savable available")
        }
        savable?.save(this)
        // Re-set property in order to post CurrentSavableEvent
        savable = savable
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
    protected fun configureCustomModules() {
        // empty
    }

    /** Initialisation method that is called after the [AbstractApplication] constructor has been left.*/
    protected open fun init() {
        // empty
    }
}