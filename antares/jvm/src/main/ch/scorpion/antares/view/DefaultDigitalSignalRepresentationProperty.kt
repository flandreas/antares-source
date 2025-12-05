package ch.scorpion.antares.view

import ch.scorpion.antares.model.signal.DigitalSignalRepresentation
import ch.scorpion.jabbah.edit.drawingBeanProvider
import ch.scorpion.jabbah.edit.properties.CommandPropertySwing

class DefaultDigitalSignalRepresentationProperty : CommandPropertySwing<DigitalSignalRepresentation>(
    "defaultSignalRepresentation",
    "element.property.DigitalGraphView.signalRepresentation",
    DigitalSignalRepresentation::class.java,
    drawingBeanProvider
)