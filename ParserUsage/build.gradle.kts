plugins {
    kotlin("jvm") version "2.0.0"
}

group = "com.momid"
version = "0.3.8"

repositories {
    mavenCentral()
    maven { setUrl("https://jitpack.io") }
}

dependencies {
    testImplementation(kotlin("test"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    implementation(kotlin("stdlib-jdk8"))
    implementation(rootProject)
    implementation(project(":ParserRules"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(16)
}
