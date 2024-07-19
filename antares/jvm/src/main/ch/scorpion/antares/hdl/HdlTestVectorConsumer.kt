package ch.scorpion.antares.hdl

import ch.scorpion.antares.model.signal.DigitalSignal
import ch.scorpion.antares.model.testcase.TestVector
import ch.scorpion.antares.model.testcase.TestVectorConsumer
import ch.scorpion.antares.model.testcase.Value
import ch.scorpion.jabbah.base.io.CodePrinter

internal abstract class HdlTestVectorConsumer(
    protected val out: CodePrinter,
    protected val portOrder: List<HDLPort>
) : TestVectorConsumer {

    var line: Int = 0
        protected set

    protected abstract fun printValues(values: List<Value>, isClock: Boolean, clock: DigitalSignal?)

    protected abstract fun checkNewLine()

    override fun consume(testVector: TestVector) {
        val clockedSignal = testVector.values.firstOrNull { it.type == Value.Type.CLOCKED }
        if (clockedSignal != null) {
            checkNewLine()
            printValues(testVector.values, true, clockedSignal.value.not())
            checkNewLine()
            printValues(testVector.values, true, clockedSignal.value)
        }
        checkNewLine()
        printValues(testVector.values, false, null)
    }
}