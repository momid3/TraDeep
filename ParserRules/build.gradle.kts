plugins {
    kotlin("jvm") version "2.0.0"
    id("com.google.devtools.ksp") version "2.0.0-1.0.23"
}

group = "com.momid"
version = "0.3.8"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    testImplementation(kotlin("test"))
    implementation(kotlin("stdlib-jdk8"))
    implementation(rootProject)
    ksp(rootProject)
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(16)
}
