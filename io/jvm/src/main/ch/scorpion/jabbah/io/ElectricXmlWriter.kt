package ch.scorpion.jabbah.io

import electric.xml.Document
import electric.xml.Element
import ch.scorpion.jabbah.base.logger
import java.io.OutputStream
import java.util.Stack

/**
 * An [XmlWriter] for writing Electric XML [Document]s
 */
class ElectricXmlWriter(val outputStream: OutputStream) : XmlWriter {

    private val LOG by logger()

    /** Holds the XML document that is to be written to [outputStream].*/
    private val document = Document()

    private val stack = Stack<Element>()

    /** ---- [XmlWriter] interface */

    override fun isRoot(): Boolean {
        return stack.isEmpty()
    }

    override fun addElementAndDescend(name: String) {
        if (isRoot()) {
            stack.push(document.setRoot(name))
        } else {
            stack.push(stack.peek().addElement(name))
        }
    }

    override fun ascend() {
        stack.pop()
    }

    override fun flush() {
        document.write(outputStream)
    }

    override fun setAttributeValue(name: String, value: String) {
        stack.peek().setAttribute(name, value)
    }
}