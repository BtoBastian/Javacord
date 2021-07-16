import java.time.Instant

plugins {
    `java-library`
    id("com.gorylenko.gradle-git-properties") version "2.3.1"
}

repositories {
    mavenCentral()
}

dependencies {
    // For old @Generated annotation in Java 9
    // can be replaced by javax.annotation.processing.Generated if Java 9 is minimum requirement
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}

/*
 * Generate a "git.properties" file with additional information about the current
 * version and the build timestamp. Used by the Javacord class to provide static
 * methods like "Javacord.VERSION" and "Javacord.COMMIT_ID".
 */
gitProperties {
    customProperty("version", version)
    customProperty("buildTimestamp", Instant.now())
}