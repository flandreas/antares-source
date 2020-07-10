package ch.scorpion.antares.model.addressable

import ch.scorpion.antares.model.signal.Word
import ch.scorpion.jabbah.execution.SignalHandler
import ch.scorpion.jabbah.graph.model.GraphActorData
import ch.scorpion.jabbah.graph.model.vertice.VerticeCalculator
import ch.scorpion.jabbah.base.logger


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
	        ram.currentSelectedAddress = ram.getAddressInput().getIncomingSignal()!!.toInt()!!
            if (ram.getWriteInput().getIncomingSignal() == Word.of(false)) {
                read(ram, signalHandler)
            }
        }

        if (ram.getChipSelectInput().getIncomingSignal() == Word.of(false)) {
            return
        }

	    ram.currentSelectedAddress = ram.getAddressInput().getIncomingSignal()!!.toInt()!!

        if (data.changedPort === ram.getAddressInput()) {
            if (ram.getWriteInput().getIncomingSignal() == Word.of(false)) {
                // Read address changed
                read(ram, signalHandler)
            }
        }

        if (data.changedPort === ram.getWriteInput()) {
            if (ram.getWriteInput().getIncomingSignal() == Word.of(false)) {
                read(ram, signalHandler)
            } else {
                undefinedOutput(ram, signalHandler)
            }
        }

        if (data.changedPort === ram.getClockInput()!!) {
            if (ram.getClockInput()!!.getIncomingSignal() == Word.of(true) && ram.getWriteInput().getIncomingSignal() == Word.of(true)) {
                write(ram, signalHandler)
            }
        }
    }

    private fun calculateUnclocked(ram: RAM, data: GraphActorData, signalHandler: SignalHandler) {
        if (data.changedPort === ram.getClearInput()) {
            ram.clear()
            read(ram, signalHandler)
        }

        if (ram.getChipSelectInput().getIncomingSignal() != Word.of(true)) {
            undefinedOutput(ram, signalHandler)
            return
        }

	    ram.currentSelectedAddress = ram.getAddressInput().getIncomingSignal()!!.toInt()!!

        if (data.changedPort === ram.getAddressInput()) {
            readOrWrite(ram, signalHandler)
        }

        if (data.changedPort === ram.getDataPort()) {
            readOrWrite(ram, signalHandler)
        }

        if (data.changedPort === ram.getChipSelectInput()) {
            readOrWrite(ram, signalHandler)
        }

        if (data.changedPort === ram.getWriteInput()) {
            if (ram.getWriteInput().getIncomingSignal() == Word.of(false)) {
                readOrWrite(ram, signalHandler)
            } else {
                undefinedOutput(ram, signalHandler)
            }
        }
    }

    private fun readOrWrite(ram: RAM, signalHandler: SignalHandler) {
        if (ram.getWriteInput().getIncomingSignal() == Word.of(true)) {
            write(ram, signalHandler)
        } else {
            read(ram, signalHandler)
        }
    }

    private fun undefinedOutput(ram: RAM, signalHandler: SignalHandler) {
        ram.getDataPort().setOutgoingSignalBuffered(Word.undefined(ram.dataWidth), signalHandler)
    }

    private fun read(ram: RAM, signalHandler: SignalHandler) {
        val address = ram.getAddressInput().getIncomingSignal()
        val value = ram.read(address!!.toInt()!!)
        ram.getDataPort().setOutgoingSignalBuffered(Word.of(ram.dataWidth, value), signalHandler)
    }


    private fun write(ram: RAM, signalHandler: SignalHandler) {
        val address = ram.getAddressInput().getIncomingSignal()
        val signal = ram.getDataPort().getIncomingSignal()!!.toInt()

        LOG.debug("Writing into RAM: address=$address, value=$signal")

        ram.write(address!!.toInt()!!, signal!!.toLong(), signalHandler)
    }
}