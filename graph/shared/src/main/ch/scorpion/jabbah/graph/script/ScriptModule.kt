package ch.scorpion.jabbah.graph.script

import ch.scorpion.jabbah.base.AbstractModule
import ch.scorpion.jabbah.base.exception.UnsupportedOperationException

/**
 * Module configuration for the [ch.scorpion.jabbah.graph.script] module.
 */
object ScriptModule : AbstractModule() {

    var scriptEngineProvider: () -> ScriptEngine = { throw UnsupportedOperationException("ScriptEngine not configured") }

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