plugins {
    `java-library`
    `maven-publish`
    signing
    id("com.github.johnrengelman.shadow") version "2.0.4" apply false
    id("net.researchgate.release") version "2.7.0" apply false
    id("biz.aQute.bnd.builder") version "5.3.0"
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":javacord-api"))
    implementation(project(":javacord-core"))
}

allprojects {
    group = "org.javacord"

    apply(plugin = "java-library")
    java {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

task("generate") // We need to create the task so that the generation files can extend it

val releaseVersion by extra(!version.toString().endsWith("-SNAPSHOT"))

subprojects {
    val shortName by extra(name.replace("javacord-", ""))
    description = "An easy to use multithreaded library for creating Discord bots in Java ($shortName)"
}

apply(from = "gradle/jars.gradle")
apply(from = "gradle/java9.gradle")
apply(from = "gradle/tests.gradle")
apply(from = "gradle/javadoc.gradle")
apply(from = "gradle/listener-manager-generation.gradle")
apply(from = "gradle/event-dispatcher-generation.gradle")
apply(from = "gradle/readme.gradle")
apply(from = "gradle/publishing.gradle")
apply(from = "gradle/checkstyle.gradle.kts")