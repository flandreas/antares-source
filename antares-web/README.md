# Antares web editor

A full circuit editor and simulator in the browser, built on the same Kotlin core as Antares Desktop.

## Running

Requires JDK 25 (for the Kotlin/JS build), Node 20+ and pnpm.

```sh
pnpm install
pnpm kotlin        # builds the Kotlin/JS library (../build/antares/dist/js/productionLibrary)
pnpm dev           # copies libraries/samples/translations to public/data and starts Vite on :5317
```

`pnpm build` creates a static site in `dist/` that can be hosted anywhere; it needs no server.

To debug Kotlin code with readable stack traces, build the development library and start Vite with it:

```sh
(cd .. && ./gradlew :antares:jsBrowserDevelopmentLibraryDistribution)
ANTARES_KOTLIN_DEV=1 pnpm dev
```

## Architecture

- `antares/js/src/kotlin/main/io/antarescircuit/antares/web/` – the Kotlin side. `AntaresWebEditor` builds the
  same shared controllers as the desktop app (`GraphDataViewController`, `GraphFrameController`, …) with browser
  views and exposes a small JSON-based API. `WebLibrary` provides the component palette and project/circuit
  management, `WebProperties`/`WebPropertyRegistry` replace the desktop's JVM-only BeanInfo property sheets, and
  `WebLibraryPersistenceService` stores libraries in a `WebFileStore`.
- `src/core/` – the TypeScript side of the bridge: `engine.ts` wraps the Kotlin API, `fileStore.ts` keeps all files
  in memory and persists user projects to IndexedDB, `projectFile.ts` reads and writes desktop-compatible `.acp`
  project files.
- `src/ui/` – the React UI (top bar, palette, circuit list, canvas, inspector, dialogs).

The editor renders circuits on a `<canvas>` through the Kotlin `CanvasJs`; editing interactions (selection, wiring,
moving, simulation input) are handled by the shared Kotlin code, exactly as on the desktop.

## Data

System libraries, sample projects and translations are copied from the Kotlin modules by `scripts/prepare-data.mjs`.
User projects live in the browser's IndexedDB; export them as `.acp` files to back them up or to open them in
Antares Desktop.
