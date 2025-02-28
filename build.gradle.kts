import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    jacoco
    checkstyle
    id("io.freefair.lombok") version "8.6"
    id("org.springframework.boot") version "3.3.1"
    id("io.spring.dependency-management") version "1.1.5"
    id("com.github.ben-manes.versions") version "0.51.0"
    id("io.sentry.jvm.gradle") version "5.2.0"
}

group = "hexlet.code"
version = "0.0.1-SNAPSHOT"

application { mainClass.set("hexlet.code.AppApplication") }

repositories { mavenCentral() }

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-devtools")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-configuration-processor")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

    implementation("org.openapitools:jackson-databind-nullable:0.2.6")
    implementation("org.mapstruct:mapstruct:1.6.0.Beta2")
    annotationProcessor("org.mapstruct:mapstruct-processor:1.6.0.Beta2")

    implementation("org.instancio:instancio-junit:4.8.1")
    implementation("net.javacrumbs.json-unit:json-unit-assertj:3.3.0")
    implementation("net.datafaker:datafaker:2.2.2")

    runtimeOnly("com.h2database:h2:2.2.224")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(platform("org.junit:junit-bom:5.11.0-M2"))
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.0-M2")

}

sentry {
  includeSourceContext = true
  org = "algins"
  projectName = "java-spring-boot"
  authToken = System.getenv("SENTRY_AUTH_TOKEN")
}

tasks.test {
    useJUnitPlatform()
    systemProperty("spring.profiles.active", "test")
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        events = mutableSetOf(TestLogEvent.FAILED, TestLogEvent.PASSED, TestLogEvent.SKIPPED)
        showStandardStreams = true
    }
}

tasks.jacocoTestReport { reports { xml.required.set(true) } }

tasks.named("sentryBundleSourcesJava").configure {
    enabled = System.getenv("SENTRY_AUTH_TOKEN") != null
}
