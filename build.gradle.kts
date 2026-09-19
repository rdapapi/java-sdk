plugins {
    `java-library`
    jacoco
    id("com.diffplug.spotless") version "7.0.2"
    id("com.vanniktech.maven.publish") version "0.30.0"
}

group = project.property("GROUP") as String
version = project.property("VERSION_NAME") as String

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

repositories {
    mavenCentral()
}

// The canary probes production, so it gets its own source set rather than a tag inside src/test:
// `check` runs offline behind a 95% coverage gate, and neither the task nor the coverage
// verification can then reach it by accident.
sourceSets {
    create("canary") {
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}

val canaryImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.api.get(), configurations.implementation.get())
}

dependencies {
    api("com.fasterxml.jackson.core:jackson-databind:2.18.4")

    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("org.assertj:assertj-core:3.27.3")
    testImplementation("com.squareup.okhttp3:mockwebserver:4.12.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    canaryImplementation(platform("org.junit:junit-bom:5.11.4"))
    canaryImplementation("org.junit.jupiter:junit-jupiter")
    canaryImplementation("org.assertj:assertj-core:3.27.3")
    "canaryRuntimeOnly"("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    // Version.SDK feeds the User-Agent, so VersionTest compares it against the
    // build's own version rather than trusting the two to be bumped together.
    systemProperty("project.version", project.version.toString())
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

tasks.jacocoTestCoverageVerification {
    dependsOn(tasks.jacocoTestReport)
    classDirectories.setFrom(
        files(classDirectories.files.map {
            fileTree(it) {
                exclude(
                    "io/rdapapi/client/responses/**",
                    "io/rdapapi/client/Version.class",
                )
            }
        })
    )
    violationRules {
        rule {
            limit {
                counter = "LINE"
                minimum = "0.95".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}

// Deliberately not wired into `check`: it needs the network and RDAPAPI_API_KEY.
tasks.register<Test>("canary") {
    group = "verification"
    description = "Probes the production API through the SDK's public surface."
    testClassesDirs = sourceSets["canary"].output.classesDirs
    classpath = sourceSets["canary"].runtimeClasspath
    useJUnitPlatform()
    outputs.upToDateWhen { false }
    testLogging {
        events("passed", "failed")
        showStandardStreams = true
        exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
    }
}

spotless {
    java {
        googleJavaFormat("1.34.1")
        targetExclude("build/**")
    }
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL, automaticRelease = true)
    signAllPublications()
}
