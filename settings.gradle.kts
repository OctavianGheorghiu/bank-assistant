plugins {
    // Lets the Java 21 toolchain download automatically when it is not installed
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

rootProject.name = "bank-assistant"

include("accounts-service", "api-gateway")
