package io.antarescircuit.jabbah.io.module

import io.antarescircuit.jabbah.base.collection.Stack
import io.antarescircuit.jabbah.base.logger
import io.antarescircuit.jabbah.io.XmlWriter
import org.w3c.dom.Element
import org.w3c.dom.XMLDocument
import org.w3c.dom.parsing.XMLSerializer
import kotlinx.browser.document

/**
 * An [XmlWriter] for writing W3C XML DOM documents.
 *
 * @property consumer called in [flush] to consume the produced XML string.
 */
class DomXmlWriter(private val consumer: (String) -> Unit) : XmlWriter {

    private val LOG by logger(DomXmlWriter::class)

    /** Holds the XML xmlDoc that is to be created and serialized into a [String]. */
    private var xmlDoc: XMLDocument? = null

    private val stack = Stack<Element>()

    /** ---- [XmlWriter] interface */

    override fun isRoot(): Boolean {
        return stack.empty
    }

    override fun addElementAndDescend(name: String) {
        try {
            if (isRoot()) {
                xmlDoc = document.implementation.createDocument(null, name, null)
                stack.push(xmlDoc!!.documentElement!!)
            } else {
                val child = xmlDoc!!.createElement(name)
                stack.peek().appendChild(child)
                stack.push(child)
            }
        } catch(e: Throwable) {
            LOG.error("DomXmlWriter: Error while descending to $name")
        }
    }

    override fun ascend() {
        stack.pop()
    }

    override fun flush() {
        try {
            consumer.invoke(XMLSerializer().serializeToString(xmlDoc!!))
        } catch(e: Exception) {
            LOG.error("Error in flushing Document to XML string: ${e.message}")
        }
    }

    override fun setAttributeValue(name: String, value: String) {
        stack.peek().setAttribute(name, value)
    }

    override fun setText(name: String, text: String) {
        addElementAndDescend(name)
        stack.peek().textContent = text
        ascend()
    }
}