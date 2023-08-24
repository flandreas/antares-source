package ch.scorpion.antares.hdl

class HDLCircuitNode(
	val circuit: HDLCircuit
) : BuiltInNode(circuit.uuid.id)