package ch.scorpion.antares.hdl

class HDLCircuitNode(
	val circuit: HDLCircuit
) : BuiltInNode(circuit.entityName) {

	override fun rename(renaming: HDLRenaming) {
		super.rename(renaming)
		hdlEntityName = renaming.checkName(hdlEntityName)
	}
}