# Antares improvements from competitor research

This local change set improves simulation throughput, waveform recording, memory workflows, truth-table editing, and command discovery. CircuitVerse, Logisim-evolution, and Digital supplied the behavioral and architectural reference points below. The implementations are original Kotlin changes within Antares' existing module boundaries.

## Research and delivered changes

| Competitor evidence | Antares improvement |
| --- | --- |
| Logisim-evolution's [ordered simulation events](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/java/com/cburch/logisim/circuit/Propagator.java) and Digital's [reused update collections](https://github.com/hneemann/Digital/blob/master/src/main/java/de/neemann/digital/core/Model.java) | Stable binary heap instead of sorting on every insertion; a timestamp index coalesces scheduler slots without a queue scan. Same-time ordering, premature execution, and reentrant scheduling retain focused coverage. |
| Logisim-evolution's [simulation deadlines](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/java/com/cburch/logisim/circuit/Simulator.java) | Timer callbacks yield after approximately 8 ms between scheduler steps. JVM elapsed-time measurement uses monotonic `System.nanoTime()` without allocating `Date` objects. One expensive step can still exceed the budget. |
| Logisim-evolution's [bounded signal storage](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/java/com/cburch/logisim/gui/log/Signal.java) and CircuitVerse's [timing-diagram rendering and download](https://github.com/CircuitVerse/CircuitVerse/blob/master/simulator/src/plotArea.js) | Waveform retention uses a deque, with amortized constant-time extrema, binary-search sample lookup, and direct range iterators. Clearing recordings resets both time modes. VCD export makes retained digital traces usable in external waveform viewers. VCD is an Antares extension of the trace-sharing workflow. |
| Logisim-evolution's [Find Action and memory navigation release notes](https://github.com/logisim-evolution/logisim-evolution/releases/tag/v5.0.0) | Searchable command palette, keyboard navigation through circuit search matches, and paged memory navigation with validated address jumps. |
| Logisim-evolution's [raw memory images](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/resources/doc/en/html/guide/mem/mem-v2raw.html) and [plain and addressed word images](https://github.com/logisim-evolution/logisim-evolution/blob/main/src/main/resources/doc/en/html/guide/mem/mem-v3word.html) | Import for all three word formats, raw and addressed export, sparse zero runs, transactional validation, and source-independent import redo. Oversized data words are rejected to preserve Antares' width-validation behavior. |
| Digital's [table editing and clipboard workflow](https://github.com/hneemann/Digital/blob/master/src/main/java/de/neemann/digital/gui/components/table/TableDialog.java) and [primitive table storage](https://github.com/hneemann/Digital/blob/master/src/main/java/de/neemann/digital/analyse/quinemc/BoolTableByteArray.java) | Multi-cell fills and clipboard paste form one undo step. Inputs are calculated from row indices, outputs use one byte per cell, and minterms use constant-time arithmetic. |

CircuitVerse's [testbench](https://github.com/CircuitVerse/CircuitVerse/blob/master/simulator/src/testbench.js) was also examined. Antares already has extensive testcase support, so the selected changes improve existing analysis and debugging workflows instead of adding a second test runner.

## Using the additions

- **Find Action:** choose Help → Find Action, or press Ctrl/Cmd+Shift+A. Search action names, menu paths, or acronyms. Disabled actions remain disabled and checkbox commands keep their normal behavior.
- **Circuit search:** open the existing search bar. Partial matching is the default. Enter and Shift+Enter visit the next and previous matches; arrow buttons and the result count expose the same workflow. Replacing the drawing reapplies the visible query.
- **Memory navigation:** open memory contents, enter an address, and choose Go. The selected radix is used unless the address has an explicit `0x`, `0b`, or `0d` prefix. Ctrl/Cmd+G focuses the address field. Previous/next controls and Ctrl/Cmd+PageUp/PageDown move between bounded pages; Show Current reveals the active address. All four layouts can reach every supported nonnegative Int address. Snapshot replacement rebuilds and clamps the displayed page.
- **Memory interchange:** Import detects Antares, `v2.0 raw`, `v3.0 hex words plain`, and `v3.0 hex words addressed` images. Export's format selector offers Antares, raw, and addressed output. Antares preserves cell comments; Logisim exports contain values only. Unsupported byte-oriented formats report an error.
- **Truth tables:** select output cells and press 0, 1, or X to fill them. Copy exports the selected rectangle as tab-separated text. Paste accepts tab-separated, comma-separated, or space-separated 0/1/X values. Invalid rectangles are rejected before editing; a single pasted bit fills the selected output cells. Input columns remain read-only.
- **Waveform export:** connect digital oscilloscope probes, run the simulation, then pause and choose File → Export → Export Waveforms (VCD). Stopping clears recordings. Exports contain retained history only; earlier missing values are unknown. Realtime timestamps are nanoseconds; clocked-mode exports identify their synthetic time axis in a VCD comment. Undefined bits export as `z`, errors as `x`.

## Additional correctness fixes

Sparse memory reads no longer allocate empty segments. Clearing the last cell reclaims its segment, and empty ranges cannot hide later cells from iteration or export. Memory clear/import undo uses sparse snapshots, preserving comments without expanding gaps. Compressed circuit persistence also writes zero gaps as runs and skips them while reading, so sparse high addresses do not require walking every intervening cell. Failed imports leave both memory and the previous data-source path intact; redo uses the validated snapshot even if the source file is edited or deleted.

Paged memory models leave partial-row padding blank and read-only, address comments and disassembly relative to the current page, and keep comments editable in disassembly layouts. Search and paging state refresh after drawing or memory snapshot replacement. English and German labels cover the new controls.

## Verification

Use JDK 25 and the repository's JVM tasks. The implementation includes focused tests for heap ordering and complexity, scheduler coalescing and reentrancy, timer fairness, history retention and extrema, memory interchange and atomicity, sparse undo/redo, address paging and snapshot restoration, clipboard validation, truth-table persistence and selection behavior, command discovery and accessibility, and search navigation.

Validation completed with `JAVA_HOME=/opt/homebrew/opt/openjdk@25/libexec/openjdk.jdk/Contents/Home ./gradlew jvmTest`: **2,571 passed, 21 skipped, no failures or errors** across all JVM modules. `git diff --check` passes. A generated VCD was also parsed successfully with the independent PyVCD reader, including a numeric probe label and mixed defined, unknown, and high-impedance bus bits.

Sol-loop review also added coverage for cancelling stale memory cell editors on snapshot restoration and preserving validation errors through command-manager rollback of rejected imports. GPT-6.1 Sol at xhigh effort approved both fixes in round 2, with no open findings. One reviewer ran in each round. No commits or pushes were made.

An existing UI test fixture also receives proper teardown: its leaked controller could intercept later library-open requests when test discovery order changed. The explicit previously failing order passes after disposing those controllers.
