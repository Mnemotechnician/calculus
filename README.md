# Kotlin mindustry mod template
My own template. Differs from the official template, uses kotlin gradle dsl.

# If you're going to use this template, make sure you follow these steps:
- [ ] Change the root project name in [settings.gradle.kts](settings.gradle.kts)
- [ ] Read the comments in [build.gradle.kts](mod-src/build.gradle.kts) and perform any required modifications
- [ ] Rename [the source code folders](mod-src/src/main/kotlin). For example, if you're using the github-based naming convention, `com/github/mnemotechnician/kmmt` should become `com/github/YOUR_USER_NAME/YOUR_REPO_OR_MOD_NAME`. You may want to use a different convention.
- [ ] Change the package path of the example mod class
- [ ] Change values in [mod.hjson](mod.hjson) to whatever you want
- [ ] After uploading the mod to github, add `mindustry-mod`, `mindustry-mod-v6` or `mindustry-mod-v7` to the repository topics in order for your mod to appear in the mod browser

# Building
## Via github actions (this is the best variant for beginners)
1. Clone the repository (this requires a GitHub account, of course)
2. Perform any necessary changes
3. Push a commit
4. Go to the "actions" tab and click on the latest run block.

If the build was successful, you will see a green indicator and a ZIPPED artifact that contains a .jar file. You should download this artifact __and unzip it__. This is a github limitation, you can't just upload a file as artifact.

If the build was unsuccessful (in 99% cases that means you made a mistake in code) you can click on the "building" job to see the log and understand what caused the error.

Note: you can use github actions without any limitations in public repositories, but in private repositories the total execution time is limited to 2000 minutes per account (for free plan users)

## Building locally
If you use this approach, I assume you use linux and have the necessary experience in using the terminal

Before building, you have to prepare the environment:
1. Make sure you have `git` and `d8` (in case of multiplatform building) installed.
2. run `git clone --depth 0 https//github.com/Mnemotechnician/kotlin-mindustry-mod-template` (replace the user and repo with your ones)
3. run `cd kotlin-mindustry-mod-template` (or wherever it was cloned into)

Now, you can compile either for desktop only or for both mobile and desktop

## Building a multiplatform jar
* Run `./gradlew release`

It will warn you that several `java.util.`, `java.time.` and `java.nio.` classes that kotlin sdk depends on are missing but you shouldn't care about that: android doesn't support them. You shouldn't be using them at all: arc provides alternatives for these classes.

In case of succeful building a jar file named `compiled-mod-any-platform.jar` (the name depends on jarName string defined in [build.gradle.kts](mod-src/build.gradle.kts)) will appear in [mod-src/build/libs/](mod-src/build/libs/).
This file can be used both on android and on desktop.

## Building a desktop-only jar
* Run `./gradlew jar`

Everything is similar to the previous paragraph, except that the file will be named `compiled-mod-desktop.jar` and will __only be usable on desktop__.

***__You should never upload this file as a release. At least a public one. There's a lot of mobile users in the community and they won't be able to use your mod at all: it will be installable but will not load__***