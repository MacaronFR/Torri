import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget

plugins {
	alias(libs.plugins.kotlinMultiplatform)
	alias(libs.plugins.androidMultiplatformLibrary)
	alias(libs.plugins.composeMultiplatform)
	alias(libs.plugins.composeCompiler)
	alias(libs.plugins.ksp)
	alias(libs.plugins.room)
	kotlin("plugin.serialization") version "2.1.20"
}

kotlin {
	listOf(
		iosArm64(),
		iosSimulatorArm64()
	).forEach { iosTarget ->
		iosTarget.binaries.framework {
			baseName = "Shared"
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

	android {
		namespace = "fr.imacaron.torri.shared"
		compileSdk = libs.versions.android.compileSdk.get().toInt()
		minSdk = libs.versions.android.minSdk.get().toInt()

		compilerOptions {
			jvmTarget = JvmTarget.JVM_11
		}
		androidResources {
			enable = true
		}
		withHostTest {
			isIncludeAndroidResources = true
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

dependencies {
	androidRuntimeClasspath(libs.compose.uiTooling)
	add("kspAndroid", libs.room.compiler)
	add("kspIosSimulatorArm64", libs.room.compiler)
	add("kspIosArm64", libs.room.compiler)
}

room {
	schemaDirectory("$projectDir/schemas")
}

compose.resources {
	publicResClass = true
	packageOfResClass = "fr.imacaron.torri.resources"
	generateResClass = always
}