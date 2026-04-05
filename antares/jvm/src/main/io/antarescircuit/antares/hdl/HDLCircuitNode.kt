package io.antarescircuit.antares.hdl

import io.antarescircuit.jabbah.base.Translations

class HDLCircuitNode(
	val circuit: HDLCircuit
) : BuiltInNode(circuit.entityName, Translations.getString("library.element.SubGraphVerticeRef.name")) {

	override fun rename(renaming: HDLRenaming) {
		super.rename(renaming)
		hdlEntityName = renaming.checkName(hdlEntityName)
	}
}