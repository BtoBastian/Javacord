plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    // For old @Generated annotation in Java 9
    // can be replaced by javax.annotation.processing.Generated if Java 9 is minimum requirement
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}