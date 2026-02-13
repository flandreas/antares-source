package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.signal.DigitalSignalFactory
import ch.scorpion.jabbah.base.logger
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator


/**
 * A [VerticeCalculator] for [RAM].
 */
class RAMCalculator : VerticeCalculator<RAM> {

    companion object {
        val LOG by logger(RAMCalculator::class)
    }

    override fun calculate(vertice: RAM, data: GraphActorData, signalHandler: SignalHandler) {
        if (vertice.hasClock) {
            calculateClocked(vertice, data, signalHandler)
        } else {
            calculateUnclocked(vertice, data, signalHandler)
        }
    }

    private fun calculateClocked(ram: RAM, data: GraphActorData, signalHandler: SignalHandler) {
        if (data.changedPort=== ram.getClearInput()) {
            ram.clear()
            read(ram, signalHandler)
        }

        if (data.changedPort === ram.getChipSelectInput()) {
	        if (!ram.isSelected) {
                undefinedOutput(ram, signalHandler)
                return
            }
	        val addressInt: Int? = ram.getAddressInput().getIncomingSignal()?.toInt()
	        if (addressInt == null) {
		        undefinedOutput(ram, signalHandler)
		        return
	        }
	        ram.currentSelectedAddress = addressInt
            if (ram.isRead) {
                read(ram, signalHandler)
            }
        }

        if (ram.isChipNotSelected) {
            return
        }

	    val addressInt: Int? = ram.getAddressInput().getIncomingSignal()?.toInt()
	    if (addressInt == null) {
		    undefinedOutput(ram, signalHandler)
		    return
	    }
	    ram.currentSelectedAddress = addressInt

        if (data.changedPort === ram.getAddressInput()) {
            if (ram.isRead) {
                // Read address changed
                read(ram, signalHandler)
            }
        }

        if (data.changedPort === ram.getWriteInput()) {
            if (ram.isRead) {
                read(ram, signalHandler)
            } else {
                undefinedOutput(ram, signalHandler)
            }
        }

        if (data.changedPort === ram.getClockInput()!!) {
            if (ram.getClockInput()!!.getIncomingSignal() == DigitalSignalFactory.of(true) && ram.isWrite) {
                write(ram, signalHandler)
            }
        }
    }

    private fun calculateUnclocked(ram: RAM, data: GraphActorData, signalHandler: SignalHandler) {
        if (data.changedPort === ram.getClearInput()) {
            ram.clear()
            read(ram, signalHandler)
        }

        if (ram.isChipSelected) {
            undefinedOutput(ram, signalHandler)
            return
        }

	    val addressInt: Int? = ram.getAddressInput().getIncomingSignal()?.toInt()
	    if (addressInt == null) {
		    undefinedOutput(ram, signalHandler)
		    return
	    }
	    ram.currentSelectedAddress = addressInt

	    if (data.changedPort === ram.getAddressInput()) {
		    readOrWrite(ram, signalHandler)
	    } else if (data.changedPort === ram.getDataPort()) {
		    readOrWrite(ram, signalHandler)
	    } else if (data.changedPort === ram.getChipSelectInput()) {
		    readOrWrite(ram, signalHandler)
	    } else if (data.changedPort === ram.getWriteInput()) {
            if (ram.isRead) {
                readOrWrite(ram, signalHandler)
            } else {
                undefinedOutput(ram, signalHandler)
            }
        } else if (ram.separateDataPorts && data.changedPort === ram.getDataInput()) {
            readOrWrite(ram, signalHandler)
        } else if (data.changedPort === null) {
	    	if (ram.isRead) {
	    		read(ram, signalHandler)
		    }
	    }
    }

    private fun readOrWrite(ram: RAM, signalHandler: SignalHandler) {
        if (ram.isWrite) {
            write(ram, signalHandler)
        } else {
            read(ram, signalHandler)
        }
    }

    private fun undefinedOutput(ram: RAM, signalHandler: SignalHandler) {
        ram.getDataPort().setOutgoingSignalBuffered(DigitalSignalFactory.undefined(ram.dataWidth), signalHandler)
    }

    private fun read(ram: RAM, signalHandler: SignalHandler) {
        val addressInt = ram.getAddressInput().getIncomingSignal()!!.toInt()
	    val value = if (addressInt != null) {
			DigitalSignalFactory.of(ram.dataWidth, ram.read(addressInt))
		}  else {
			DigitalSignalFactory.undefined(ram.dataWidth)
	    }
	    ram.getDataPort().setOutgoingSignalBuffered(value, signalHandler)
    }

    private fun write(ram: RAM, signalHandler: SignalHandler) {
        val address = ram.getAddressInput().getIncomingSignal()
        val data = ram.getEffectiveDataInput().getIncomingSignal()

	    val addressInt = address!!.toInt()
	    val dataInt = data!!.toInt()

	    if (addressInt != null && dataInt != null) {
            LOG.trace("Writing into RAM: address=$addressInt, value=$dataInt")
		    ram.write(addressInt, dataInt.toULong(), signalHandler)
            if (ram.separateDataPorts) {
                ram.getDataPort().setOutgoingSignalBuffered(data, signalHandler)
            }
	    }
    }
}