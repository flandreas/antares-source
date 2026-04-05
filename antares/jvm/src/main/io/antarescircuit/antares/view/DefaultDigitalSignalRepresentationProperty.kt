package io.antarescircuit.antares.view

import io.antarescircuit.antares.model.signal.DigitalSignalRepresentation
import io.antarescircuit.jabbah.edit.drawingBeanProvider
import io.antarescircuit.jabbah.edit.properties.CommandPropertySwing

class DefaultDigitalSignalRepresentationProperty : CommandPropertySwing<DigitalSignalRepresentation>(
    "defaultSignalRepresentation",
    "element.property.DigitalGraphView.signalRepresentation",
    DigitalSignalRepresentation::class.java,
    drawingBeanProvider
)