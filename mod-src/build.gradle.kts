/*
 * This file allows you to configure the compilation process of your mod
 * There's several comments that you should read & follow in order to make everything work correctly
 */

plugins {
	id("org.jetbrains.kotlin.jvm") version "1.6.10"
	
	/**
	 * Uncomment this line and the "publications" block below if you want to publish to maven. 
	 * Normally you don't need that unless you're creating a library.
	*/
	//`maven-publish`
}

/** The mindustry version this mod will be compiled for. You may want to use another version, e.g. "v126" or "v145" (when it'll come out) */
val mindustryVersion = "v135"
/** The output jar files will contain this string in their names. If you're going to modify it, you should also modify the name in project_dir/.github/workflows/build.yml */
val jarName = "compiled-mod"

repositories {
	mavenCentral()
	maven("https://jitpack.io")
}

dependencies {
	/*
	 * You can add your mod dependencies in this block.
	 * NEVER ADD MINDUSTRY, ARC AND NON-LIBRARY MODS AS IMPLEMENTATION DEPENDENCIES! Use compileOnly instead (and add a dependency in mod.hjson if it's a mod)
	*/
	implementation(platform("org.jetbrains.kotlin:kotlin-bom"))
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	
	compileOnly("com.github.Anuken.Arc:arc-core:$mindustryVersion")
	compileOnly("com.github.Anuken.Mindustry:core:$mindustryVersion")
	
	//example of a library dependency. if you don't need it, remove this line.
	//(note: this is not a mod, rather a library for mindustry mods, thus it should be added as an implementation)
	implementation("com.github.mnemotechnician:mkui:snapshot-4")
}

/*
 * Read the comment in "plugins" block.

publishing {
	publications {
		create<MavenPublication>("maven") {
			groupId = "com.github.YOUR_GITHUB_USERNAME" //replace these with your username/reponame
			artifactId = "YOUR_GITHUB_REPO_NAME"
			version = "1.0"

			from(components["java"])
		}
	}
}

*/

/** Android-specific stuff. Do not modify unless you're 100% sure you know what you're doing! If you break this task, mobile users won't be able to use your mod!*/
task("jarAndroid") {
	dependsOn("jar")
	
	doLast {
		val sdkRoot = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
		
		if(sdkRoot == null || sdkRoot.isEmpty() || !File(sdkRoot).exists()) {
			throw GradleException("""
				No valid Android SDK found. Ensure that ANDROID_HOME is set to your Android SDK directory.
				Note: if the gradle daemon has been started before ANDROID_HOME env variable was defined, it won't be able to read this variable.
				In this case you have to run "./gradlew stop" and try again
			""".trimIndent());
		}
		
		println("searching for an android sdk... ")
		val platformRoot = File("$sdkRoot/platforms/").walkTopDown().findLast { 
			val fi = File(it, "android.jar")
			if (fi.exists()) {
				print(it)
				println(" — OK.")
			}
			fi.exists()
		}
		
		if (platformRoot == null) throw GradleException("No android.jar found. Ensure that you have an Android platform installed. (platformRoot = $platformRoot)")
		
		//collect dependencies needed to translate java 8+ bytecode code to android-compatible bytecode (yeah, android's dvm and art do be sucking)
		val dependencies = (configurations.compileClasspath.files + configurations.runtimeClasspath.files + File(platformRoot, "android.jar")).map { it.path }
		val dependenciesStr = Array<String>(dependencies.size * 2) {
			if (it % 2 == 0) "--classpath" else dependencies.elementAt(it / 2)
		}
		
		//dexing. As a result of this process, a .dex file will be added to the jar file. This requires d8 tool in your $PATH
		exec {
			workingDir("$buildDir/libs")
			commandLine("d8", *dependenciesStr, "--min-api", "14", "--output", "${jarName}-android.jar", "${jarName}-desktop.jar")
		}
	}
}

/** Merges the dektop and android jar files into a multiplatform jar file */
task<Jar>("release") {
	dependsOn("jarAndroid")
	
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	archiveFileName.set("${jarName}-any-platform.jar")

	from(
		zipTree("$buildDir/libs/${jarName}-desktop.jar"),
		zipTree("$buildDir/libs/${jarName}-android.jar")
	)

	doLast {
		delete {
			delete("$buildDir/libs/${jarName}-desktop.jar")
			delete("$buildDir/libs/${jarName}-android.jar")
		}
	}
}


tasks.jar {
	duplicatesStrategy = DuplicatesStrategy.EXCLUDE
	archiveFileName.set("${jarName}-desktop.jar")

	from(*configurations.runtimeClasspath.files.map { if (it.isDirectory()) it else zipTree(it) }.toTypedArray())

	from(rootDir) {
		include("mod.hjson")
		include("icon.png")
	}

	from("../assets/") {
		include("**")
	}
}