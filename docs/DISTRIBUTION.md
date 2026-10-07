# Distribution and jpackage

The current Maven application is a developer build, not a standalone installer. On JDK 21, use `jpackage` with the app jar and a Java runtime image. Bundle the matching GnuCOBOL executable and runtime libraries for the target architecture, preserve licenses, and test on a clean Windows machine without development tools. This packaging work remains pending.
