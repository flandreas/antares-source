package io.antarescircuit.jabbah.io

import electric.xml.Document
import electric.xml.Element
import io.antarescircuit.jabbah.base.logger
import java.io.InputStream
import java.util.*

/**
 * An [XmlReader] for reading Electric XML [Document]s.
 */
class ElectricXmlReader(inputStream: InputStream) : XmlReader {

	companion object {
        private val LOG by logger(ElectricXmlReader::class)
	}

    /** Holds the XML document that has been read from [inputStream] and parsed by Electric XML.*/
    private val document = Document(inputStream)

    private val stack = Stack<Element>()

    /**
     * Caches the child elements of elements on [stack] that have been descended into by index, since
     * [Element.getElementAt] walks the linked child list on each call.
     */
    private val childElements = IdentityHashMap<Element, List<Element>>()

    init {
        stack.push(document.root)
    }

    /** ---- [XmlReader] interface */

    override fun getName(): String {
        if (LOG.isTraceEnabled()) {
            LOG.trace("getName '${stack.peek().name}'")
        }
        return stack.peek().name
    }

    override fun getAttributeValue(name: String): String {
        return stack.peek().getAttributeValue(name) ?: throw IllegalArgumentException("mandatory attribute '$name' not found")
    }

    override fun hasAttribute(name: String): Boolean {
        return stack.peek().getAttributeValue(name) != null
    }

    override fun hasElement(name: String): Boolean {
        return stack.peek().getElement(name) != null
    }

    override fun getElementsCount(): Int {
        return stack.peek().elements.size()
    }

    override fun descend(name: String) {
        if (LOG.isTraceEnabled()) {
            LOG.trace("descend to '$name'")
        }
        stack.push(stack.peek().getElement(name))
    }

    override fun descend(index: Int) {
        val parent = stack.peek()
        if (LOG.isTraceEnabled()) {
            LOG.trace("descend to index $index of '${parent.name}'")
        }
        stack.push(childElements.getOrPut(parent) { collectElements(parent) }[index - 1])
    }

    override fun ascend() {
        if (LOG.isTraceEnabled()) {
            LOG.trace("ascend from '${stack.peek().name}'")
        }
        childElements.remove(stack.pop())
    }

    override fun getElementNames(): Set<String> =
        collectElements(stack.peek()).mapTo(mutableSetOf()) { it.name }

    override fun getText(name: String): String = stack.peek().getString(name)

    /** ---- [ElectricXmlReader] */

    private fun collectElements(parent: Element): List<Element> {
        val e = parent.elements
        val elements = mutableListOf<Element>()
        while (e.hasMoreElements()) {
            elements.add(e.current())
            e.next()
        }
        return elements
    }
}