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
  
Make sure that all changes from older release branches have been merged to the master.

Clean and build the release.

Increment the current release number and update it at the following
   locations:
  
> **_TODO:_** The release number should exist at only one single place.
  
* `gradle.properties`
* `antares/shared/rsc/version.txt`
 
Commit and push to remote repository.

Tag the release and push it to the remote repository:

* `git tag -a v<version> -m "my version <version>"`
* `git push origin --tags`

### Build Installers

* On a macOS machine
  * `gradlew clean :antares:distributeMac`
  * Wait for notarization success mail from Apple (or check manually using `xcrun altool 
    --notarization-info`)
  * `gradlew stapleMacNotarization`
  * Collect the macOS package `build/antares/distributions/Antares-<version>.dmg`
  * Collect the ProGuard mapping file in `build/antares/libs/antares-<version>-proguard.map` and
   store it in a save place. You will need it for un-obfuscating stack traces from bug reports.
* On a windows machine (after pulling changes from remote repository)
  * `gradlew clean :antares:distributeWindows`
  * Collect the Windows package `build/antares/distributions/Antares-<version>.msi`
  
Install the releases and do some some testing.

### Build User Manual

The asciidoc source files are copied to the website source and processed there by jekyll.

* Create a ZIP file of `doc/usermanual/english`: english.zip
* Create a ZIP file of `doc/images/user-manual`: user-manual.zip

### Build examples

* Export all sample projects from the project dialog in Antares.
 
## Deploying
 
Deployment is done by pushing all artifacts to git@github.com:flandreas/antares.git.

### Assets (Installers and examples)

* Create a new release in the github project, e.g. "Release 0.3.0"
* Upload the two installers as attachments to the release
* Upload the example projects (zip) as attachments to the release
    * Make sure that download buttons in example pages reference the correctly versioned URL
* Save as "draft"

### Web site

#### User Manual

* Unpack english.zip in directory `user-manual`
* Unpack user-manual.zip in directory `assets/images`

#### Releases page

* Update file "version.txt" used by check for newest version
* Add new release page and list all closed issues
* Add the new release page in `_data/navigation.yml`
* Reference the new release packages in `quick-start.md`
* Reference the new release page in `index.md`
* New blog page with release announcement

Commit, push and tag the website repo with release version (e.g. "v0.4.0"). After that, the "Release
" on the
 GitHub project can be released using the committed tag.
 
### Deployment test

Delete your local release, download the new release from the website, download a sample project
, import it, and do some smoke testing.

## Support

### Retracing stack traces

Stacktraces from Antares log files contain obfuscated identifiers. Use ProGuard's `retrace.sh
` and the mapping file (collected after build) to translate a stack trace (which has been stored
 in a text file) into readable form.
 
`retrace.sh antares-0.2.0-proguard.map stacktrace.txt`