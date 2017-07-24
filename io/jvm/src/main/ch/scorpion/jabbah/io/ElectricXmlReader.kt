package ch.scorpion.jabbah.io

import electric.xml.Document
import electric.xml.Element
import ch.scorpion.jabbah.base.logger
import java.io.InputStream
import java.util.*

/**
 * An [XmlReader] for reading Electric XML [Document]s.
 */
class ElectricXmlReader(inputStream: InputStream) : XmlReader {

    private val LOG by logger(ElectricXmlReader::class)

    /** Holds the XML document that has been read from [inputStream] and parsed by Electric XML.*/
    private val document = Document(inputStream)

    private val stack = Stack<Element>()

    init {
        stack.push(document.root)
    }

    /** ---- [XmlReader] interface */

    override fun getName(): String {
        LOG.trace("getName '${stack.peek().name}'")
        return stack.peek().name
    }

    override fun getAttributeValue(name: String): String {
        return stack.peek().getAttributeValue(name)
    }

    override fun hasAttribute(name: String): Boolean {
        return stack.peek().getAttributeValue(name) != null
    }

    override fun hasElement(name: String): Boolean {
        return stack.peek().getElement(name) != null
    }

    override fun getElementsCount(): Int {
        return stack.peek().getElements().size()
    }

    override fun descend(name: String) {
        LOG.trace("descend to '$name'")
        stack.push(stack.peek().getElement(name))
    }

    override fun descend(index: Int) {
        LOG.trace("descend to index $index of '${stack.peek().name}'")
        stack.push(stack.peek().getElementAt(index))
    }

    override fun ascend() {
        LOG.trace("ascend from '${stack.peek().name}'")
        stack.pop()
    }
}