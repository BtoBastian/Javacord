plugins {
    `java-library`
}

repositories {
    mavenCentral()
}

dependencies {
    api(project(":javacord-api")) {
        isTransitive = true
    }

    // OkHttp for REST-calls
    implementation("com.squareup.okhttp3:okhttp:3.9.1")
    implementation("com.squareup.okhttp3:logging-interceptor:3.9.1")

    // the JSON-lib because Discord returns in JSON format
    implementation("com.fasterxml.jackson.core:jackson-databind:2.9.3")

    // the web socket
    implementation("com.neovisionaries:nv-websocket-client:2.6")

    // voice encryption
    implementation("com.codahale:xsalsa20poly1305:0.10.1")

    // logging
    implementation("org.apache.logging.log4j:log4j-api:2.11.0")

    // Vavr, mainly for immutable collections
    // We are using 0.10.1, because of an issue in 0.10.2: https://github.com/vavr-io/vavr/issues/2573
    implementation("io.vavr:vavr:0.10.1")

    // For old @Generated annotation in Java 9
    // can be replaced by javax.annotation.processing.Generated if Java 9 is minimum requirement
    compileOnly("javax.annotation:javax.annotation-api:1.3.2")
}
