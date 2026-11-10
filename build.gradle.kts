import org.springframework.boot.gradle.plugin.SpringBootPlugin

// Pinned versions, recorded in docs/state/repository.md
plugins {
    id("org.springframework.boot") version "4.1.2" apply false
}

val springCloudVersion = "2025.1.4"

subprojects {
    apply(plugin = "java")
    apply(plugin = "org.springframework.boot")

    group = "com.bankassistant"

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        toolchain.languageVersion = JavaLanguageVersion.of(21)
    }

    dependencies {
        "implementation"(platform(SpringBootPlugin.BOM_COORDINATES))
        "implementation"(platform("org.springframework.cloud:spring-cloud-dependencies:$springCloudVersion"))

        // Lombok in every service (docs/design/code-conventions.md); version managed by the Spring Boot BOM
        "compileOnly"(platform(SpringBootPlugin.BOM_COORDINATES))
        "annotationProcessor"(platform(SpringBootPlugin.BOM_COORDINATES))
        "compileOnly"("org.projectlombok:lombok")
        "annotationProcessor"("org.projectlombok:lombok")

        "testImplementation"("org.springframework.boot:spring-boot-starter-test")
        "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
    }

    // Only the runnable boot jar, so each Dockerfile copies exactly one jar
    tasks.named("jar") { enabled = false }

    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
