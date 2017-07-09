package ch.scorpion.jabbah.io.module

import ch.scorpion.jabbah.base.collection.Stack
import ch.scorpion.jabbah.base.loggerFor
import ch.scorpion.jabbah.io.XmlWriter
import org.w3c.dom.Element
import org.w3c.dom.XMLDocument
import org.w3c.dom.parsing.XMLSerializer

/**
 * An [XmlWriter] for writing W3C XML DOM documents.
 *
 * @property consumer called in [flush] to consume the produced XML string.
 */
class DomXmlWriter(private val consumer: (String) -> Unit) : XmlWriter {

    private val LOG by loggerFor(this)

    /** Holds the XML document that is to be created and serialized into a [String]. */
    private val document = XMLDocument()

    private val stack = Stack<Element>()

    /** ---- [XmlWriter] interface */

    override fun isRoot(): Boolean {
        return stack.empty
    }

    override fun addElementAndDescend(name: String) {
        val child = document.createElement(name)
        if (isRoot()) {
            document.rootElement!!.appendChild(child)
        } else {
            stack.peek().appendChild(child)
        }
        stack.push(child)
    }

    override fun ascend() {
        stack.pop()
    }

    override fun flush() {
        try {
            consumer.invoke(XMLSerializer().serializeToString(document))
        } catch(e: Exception) {
            LOG.error("Error in flushing Document to XML string: ${e.message}")
        }
    }

    override fun setAttributeValue(name: String, value: String) {
        stack.peek().setAttribute(name, value)
    }
}