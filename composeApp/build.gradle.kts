import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidApplication)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.ksp)
	alias(libs.plugins.room)
	kotlin("plugin.serialization") version "2.1.20"
}

kotlin {
	androidTarget {
		@OptIn(ExperimentalKotlinGradlePluginApi::class)
		compilerOptions {
			jvmTarget.set(JvmTarget.JVM_11)
		}
	}

	listOf(
		iosX64(),
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "ComposeApp"
			isStatic = true
		}

		iosTarget.compilations.getByName("main") {
			val SumUp by cinterops.creating {
				definitionFile.set(project.file("SumUpSDK.def"))
				compilerOpts("-framework", "SumUpSDK", "-F/$rootDir/SumUpSDK.xcframework/ios-arm64_x86_64-simulator")
			}
			cinterops.create("NearbySwift") {
				definitionFile.set(file("Nearby.def"))
				includeDirs.allHeaders(rootDir.resolve("iosApp/iosApp/bridge"))
			}
			cinterops.create("OpenFile") {
				definitionFile.set(file("OpenFile.def"))
				includeDirs.allHeaders(rootDir.resolve("iosApp/iosApp/bridge"))
			}
		}
		iosTarget.binaries.all {
			linkerOpts("-framework", "SumUpSDK", "-F/$rootDir/SumUpSDK.xcframework/ios-arm64_x86_64-simulator")
		}
	}

	sourceSets {

		androidMain.dependencies {
			implementation(libs.ui.tooling.preview)
			implementation(libs.androidx.activity.compose)
			implementation(libs.merchant.sdk)
			implementation(libs.play.services.nearby.android)
		}
		commonMain.dependencies {
			implementation(libs.runtime)
			implementation(libs.foundation)
			implementation(libs.material3)
			implementation(libs.ui)
			implementation(libs.animation)
			implementation(libs.components.resources)
			implementation(libs.ui.tooling.preview)
			implementation(libs.androidx.lifecycle.viewmodel)
			implementation(libs.androidx.lifecycle.runtimeCompose)
			implementation(libs.room.runtime)
			implementation(libs.sqlite)
			implementation(libs.ktor.client.core)
			implementation(libs.ktor.client.cio)
			implementation(libs.ktor.client.content.negotiation)
			implementation(libs.ktor.serialization.kotlinx.json)
			implementation(libs.icons.lucide)
			implementation(libs.lifecycle.viewmodel.compose)
			implementation(libs.datastore)
			implementation(libs.datastore.preferences)
			implementation(libs.adaptive)
			implementation(libs.navigation.compose)
			implementation(libs.kotlinx.datetime)
			implementation(libs.oidc.appsupport)
			implementation(libs.oidc.ktor)
		}
		iosMain.dependencies {
			implementation(libs.ktor.client.darwin)
		}
	}
}

android {
	namespace = "fr.imacaron.torri"
	compileSdk = libs.versions.android.compileSdk.get().toInt()

	defaultConfig {
		applicationId = "fr.imacaron.torri"
		minSdk = libs.versions.android.minSdk.get().toInt()
		targetSdk = libs.versions.android.targetSdk.get().toInt()
		versionCode = 12
		versionName = "1.8.1"
		addManifestPlaceholders(
			mapOf("oidcRedirectScheme" to "fr.imacaron.torri")
		)
	}
	packaging {
		resources {
			excludes += "/META-INF/{AL2.0,LGPL2.1}"
		}
	}
	buildTypes {
		getByName("release") {
			isMinifyEnabled = false
		}
	}
	compileOptions {
		isCoreLibraryDesugaringEnabled = true
		sourceCompatibility = JavaVersion.VERSION_11
		targetCompatibility = JavaVersion.VERSION_11
	}
}

dependencies {
	coreLibraryDesugaring(libs.desugar.jdk.libs)
	debugImplementation(libs.ui.tooling)
	add("kspAndroid", libs.room.compiler)
	add("kspIosSimulatorArm64", libs.room.compiler)
	add("kspIosX64", libs.room.compiler)
	add("kspIosArm64", libs.room.compiler)
}

room {
	schemaDirectory("$projectDir/schemas")
}