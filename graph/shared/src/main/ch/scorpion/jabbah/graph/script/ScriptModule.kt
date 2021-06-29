package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.base.AbstractModule

/**
 * Module configuration for the [ch.scorpion.jabbah.graph.script] module.
 */
object ScriptModule : AbstractModule() {

    var scriptGatewayProvider: () -> ScriptGateway = { throw UnsupportedOperationException("ScriptGateway not configured") }

    private var _scriptGateway: ScriptGateway? = null
    val scriptGateway: ScriptGateway
        get() {
            if (_scriptGateway == null) {
                _scriptGateway = scriptGatewayProvider.invoke()
            }
            return _scriptGateway!!
        }

    override fun initialize() {
        // empty
    }

    /** Used for testing. */
    fun resetScriptGateway() {
        _scriptGateway = null
    }
}