package io.antarescircuit.antares.filebased

import io.antarescircuit.jabbah.base.UUID

/**
 * 3-bit frequency divider built from T-flipflops with negative edge-triggering.
 */
class FrequencyDividerTNegTest : AbstractFrequencyDividerTest(
    UUID("dff6c785-8dd4-4ece-8c54-0252cd09fa4d"),
    16,
    7
)