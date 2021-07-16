import java.math.BigInteger
import java.security.MessageDigest

tasks.register("verifyReadme") {
    inputs.file("README.md").withPropertyName("readme")
    inputs.file(".github/README.md.sha256").withPropertyName("readmeChecksum")

    doLast {
        if (file(".github/README.md.sha256").readText() != calculateReadmeChecksum()) {
            if (gradle.taskGraph.hasTask("updateReadme")) {
                throw IllegalStateException("The README.md file was tampered with manually, " +
                        "if you want to overwrite it, add \"-x $name\" to your Gradle call")
            }
            throw IllegalStateException("The README.md file was tampered with manually." +
                    " Please modify the \"./github/README_template.md\" file instead.")
        }
    }
}

tasks["check"].dependsOn("verifyReadme")

tasks.register("updateReadme") {
    val coreRuntimeClasspath = project(":javacord-core").configurations.runtimeClasspath

    inputs.property("version", version)
    inputs.file(".github/README_template.md").withPropertyName("readmeTemplate")
    inputs.files(coreRuntimeClasspath)
        .withPropertyName("coreRuntimeClasspath")
        .withNormalizer(ClasspathNormalizer::class)

    outputs.file("README.md").withPropertyName("readme")
    outputs.file(".github/README.md.sha256").withPropertyName("readmeChecksum")

    doLast {
        val log4jVersion = coreRuntimeClasspath.get()
            .allDependencies
            .find { it.group == "org.apache.logging.log4j" && it.name == "log4j-api" }
            ?.version

        copy {
            from(".github/README_template.md")
            into(".")
            rename { "README.md" }
            filteringCharset = Charsets.UTF_8.toString()
            expand(
                "version" to version,
                "log4jVersion" to log4jVersion
            )
        }
        file(".github/README.md.sha256").writeText(calculateReadmeChecksum())
    }
}


fun calculateReadmeChecksum(): String {
    val sha256 = MessageDigest.getInstance("SHA-256")
    val checksum = sha256.digest(file("README.md")
        .readLines(Charsets.UTF_8)
        .joinToString("\n")
        .toByteArray(Charsets.UTF_8))

    return BigInteger(1, checksum)
        .toString(16)
        .padStart(sha256.digestLength * 2, '0')
}