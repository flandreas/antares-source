package ch.scorpion.jabbah.base

/**
 * Defines a simple module system like the one of Google Guice, without injection support.
 */
interface Module {

    /** Called by an application or by other [Module]s to indicate that the depend on this [Module].*/
    fun require()
}

abstract class AbstractModule : Module {

    /** Remember whether this [Module] has already been configured or not. */
    private var configured: Boolean = false

    override fun require() {
        if (!configured) {
            initialize()
            configured = true
        }
    }

    /**
     * Initializes this [Module]. Implementations should first declare the [Module] they depend on,
     * and then configure defaults implementations of interfaces and default property values, which might
     * overwrite configurations of lower level modules.
     */
    protected abstract fun initialize()

}