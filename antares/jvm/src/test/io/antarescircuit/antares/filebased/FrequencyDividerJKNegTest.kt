package io.antarescircuit.antares.filebased

import io.antarescircuit.jabbah.base.UUID

/**
 * 3-bit frequency divider from JK-flipflops with negative edge-triggering.
 */
class FrequencyDividerJKNegTest : AbstractFrequencyDividerTest(
    UUID("619df55a-b528-4294-b29b-4f8612eff2d4"),
    30,
    28
)