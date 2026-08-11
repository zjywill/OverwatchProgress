plugins {
  alias(libs.plugins.android.library)
  `maven-publish`
}

android {
  namespace = "com.comix.overwatch"
  compileSdk = libs.versions.compileSdk.get().toInt()
  resourcePrefix = "hive_"

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
  }

  buildTypes {
    release {
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  // Replaces the hand-rolled sourcesJar/javadocJar tasks and the dead
  // com.github.dcendents.android-maven plugin.
  publishing {
    singleVariant("release") {
      withSourcesJar()
      withJavadocJar()
    }
  }
}

// JitPack injects -Pgroup and -Pversion when it builds a tag; these are only the fallbacks used
// when publishing locally. Leaving the publication coordinates unset lets both paths agree.
if (group.toString().isBlank() || group.toString() == rootProject.name) {
  group = "com.github.zjywill"
}
if (version.toString() == Project.DEFAULT_VERSION) {
  version = "2.0.0"
}

publishing {
  publications {
    register<MavenPublication>("release") {
      afterEvaluate { from(components["release"]) }
    }
  }
}

dependencies {
  testImplementation(libs.junit)
}
