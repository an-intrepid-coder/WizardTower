import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    // Note: When updating these versions, *always* look at the exact nomenclature listed on
    //       the official websites. It is the only reliable way to do it.
    kotlin("jvm") version "1.7.0"
    id("org.jetbrains.compose") version "1.2.0-alpha01-dev741"
}

group = "me.sgibber2018"
version = "1.0.7"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "WizardTower"
            packageVersion = "1.0.7"
        }
    }
}