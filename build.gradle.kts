plugins {
    kotlin("jvm") version "1.9.22"
}

group = "eu.rechenwerk.mordbeitisch"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.fasterxml.jackson.core:jackson-core:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.16.1")

    implementation("org.apache.pdfbox:pdfbox:2.0.21")

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")

}


kotlin {
    jvmToolchain(8)
}