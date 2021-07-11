subprojects {
    apply(plugin = "checkstyle")

    configure<CheckstyleExtension> {
        toolVersion = "8.9"
        maxWarnings = 0
    }
}