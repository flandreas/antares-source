package io.antarescircuit.jabbah.io

import electric.xml.Document
import electric.xml.Element
import java.io.OutputStream
import java.util.*

/**
 * An [XmlWriter] for writing Electric XML [Document]s
 */
class ElectricXmlWriter(private val outputStream: OutputStream) : XmlWriter {

    /** Holds the XML document that is to be written to [outputStream].*/
    private val document = Document()

    private val stack = Stack<Element>()

    /** ---- [XmlWriter] interface */

    override fun isRoot(): Boolean = stack.isEmpty()

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

    override fun setText(name: String, text: String) {
        stack.peek().setText(name, text)
    }
}