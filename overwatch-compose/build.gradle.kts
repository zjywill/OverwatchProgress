plugins {
  alias(libs.plugins.android.library)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.roborazzi)
  `maven-publish`
}

android {
  namespace = "com.comix.overwatch.compose"
  compileSdk = libs.versions.compileSdk.get().toInt()

  defaultConfig {
    minSdk = libs.versions.minSdk.get().toInt()
  }

  buildFeatures {
    compose = true
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

  testOptions {
    unitTests {
      isIncludeAndroidResources = true
    }
  }

  publishing {
    singleVariant("release") {
      withSourcesJar()
      withJavadocJar()
    }
  }
}

// JitPack injects -Pgroup and -Pversion; these are only the local fallbacks.
if (group.toString().isBlank() || group.toString() == rootProject.name) {
  group = "com.github.zjywill"
}
if (version.toString() == Project.DEFAULT_VERSION) {
  version = "2.1.0"
}

publishing {
  publications {
    register<MavenPublication>("release") {
      afterEvaluate { from(components["release"]) }
    }
  }
}

dependencies {
  // api: HiveGeometry is part of this module's surface, and callers may want the View too.
  api(project(":overwatch"))

  implementation(platform(libs.compose.bom))
  api(libs.compose.foundation)
  implementation(libs.compose.ui)
  implementation(libs.compose.ui.tooling.preview)
  debugImplementation(libs.compose.ui.tooling)

  testImplementation(libs.junit)
  testImplementation(libs.robolectric)
  testImplementation(libs.androidx.test.junit)
  testImplementation(platform(libs.compose.bom))
  testImplementation(libs.compose.ui.test.junit4)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  debugImplementation(libs.compose.ui.test.manifest)
}
