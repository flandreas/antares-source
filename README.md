# Antares

Antares is digital circuit learning platform entirely written in Kotlin.

Antares aims for providing tools to create circuits with embedded
 explanations. The current version consists of a Swing-based desktop
  application. Future versions will also be available in the browser.
 
## Developing
 
TODO
 
## Building
 
TODO
 
## Releasing
 
### Preparation
  
Increment the current release number and update it at the following
   locations:
  
> **_TODO:_** The release number should exist at only one single place.
  
* `gradle.properties`
* `antares/shared/src/version.txt`
 
Commit and push to remote repository.

Tag the release and push it to the remote repository:

* `git tag -a v<version> -m "my version <version>"`
* `git push origin --tags`

### Build Packages

* On a macOS machine
  * `gradlew clean :antares:distributeMac`
  * Collect the macOS package `build/antares/distributions/Antares-<version>.pkg`
  * Collect the ProGuard mapping file in `build/antares/libs/antares-<version>-proguard.map` and
   store it in a save place. You will need it for un-obfuscating stack traces from bug reports.
* On a windows machine (after pulling changes from remote repository)
  * `gradlew clean :antares:distributeWindows`
  * Collect the Windows package `build/antares/distributions/Antares-<version>.msi`

### Build User Manual

* `gradlew asciidoctor`
* Create a ZIP file of all directories in `build/doc/usermanual/html5`
 
## Deploying
 
TODO

## Support

### Retracing stack traces

Stacktraces from Antares log files contain obfuscated identifiers. Use ProGuard's `retrace.sh
` and the mapping file (collected after build) to translate a stack trace (which has been stored
 in a text file) into readable form.
 
`retrace.sh antares-0.2.0-proguard.map stacktrace.txt`