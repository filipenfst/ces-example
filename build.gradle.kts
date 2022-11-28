import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("kapt") version "1.7.21"
    kotlin("jvm") version "1.7.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.21"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("io.micronaut.application") version "3.6.5"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    jacoco
}

group = "com.global.payment"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

val testcontainersVersion = "1.17.6"
val resilience4jVersion = "2.0.0"
val coroutinesVersion = "1.6.4"
val jacksonVersion = "2.13.4"
val wiremockVersion = "2.35.0"
val mockkVersion = "1.13.2"
val postgresqlVersion = "42.5.0"
val ktlint by configurations.creating

dependencies {
    kapt("io.micronaut.data:micronaut-data-processor")
    kapt("io.micronaut:micronaut-http-validation")
    kapt("io.micronaut.openapi:micronaut-openapi")
    kapt("io.micronaut:micronaut-management")
    kapt("io.micronaut:micronaut-graal")
    kapt("io.micronaut.serde:micronaut-serde-processor")

    compileOnly("org.graalvm.nativeimage:svm")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect")

    implementation("io.micronaut:micronaut-http-client")
    implementation("io.micronaut:micronaut-validation")
    implementation("io.micronaut.kotlin:micronaut-kotlin-extension-functions")
    implementation("io.micronaut.data:micronaut-data-r2dbc")
    implementation("io.micronaut.flyway:micronaut-flyway")

    implementation("io.micronaut.serde:micronaut-serde-jackson")

    implementation("io.micronaut.tracing:micronaut-tracing-zipkin")
    implementation("io.micronaut.micrometer:micronaut-micrometer-registry-prometheus")

    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-retry:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-micronaut:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-consumer:$resilience4jVersion")

    implementation("jakarta.annotation:jakarta.annotation-api")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    implementation("ch.qos.logback:logback-classic:1.4.5")
    implementation("org.slf4j:slf4j-simple")
    implementation("org.slf4j:slf4j-api")
    implementation("io.swagger.core.v3:swagger-annotations")
    implementation("io.micronaut:micronaut-management")

    runtimeOnly("io.r2dbc:r2dbc-postgresql:0.8.13.RELEASE")
    runtimeOnly("org.postgresql:postgresql:42.5.0")
    runtimeOnly("io.r2dbc:r2dbc-pool")

    ktlint("com.pinterest:ktlint:0.47.1")

    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("io.micronaut.test:micronaut-test-rest-assured")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:$wiremockVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.micronaut.test:micronaut-test-junit5")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")

    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}
//configurations.all {
//    if (name != "testResourcesService") {
//        resolutionStrategy.dependencySubstitution {
//            substitute(module("io.micronaut:micronaut-jackson-databind"))
//                .using(module("io.micronaut.serde:micronaut-serde-jackson:1.3.2"))
//        }
//    }
//}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("com.global.payment.application.*")
    }
}
application {
    mainClass.set("com.global.payment.application.ApplicationKt")
}

detekt {
    buildUponDefaultConfig = true
    allRules = false
}

val outputDir = "${project.buildDir}/reports/ktlint/"
val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))



tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestCoverageVerification)
}

val ktlintFormat by tasks.creating(JavaExec::class) {
    println("Executing ktlintFormat")
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Fix Kotlin code style deviations."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("-F", "src/**/*.kt")

    jvmArgs = listOf("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}

val ktlintCheck by tasks.creating(JavaExec::class) {
    println("Executing ktlintCheck")
    inputs.files(inputFiles)
    outputs.dir(outputDir)

    description = "Check Kotlin code style."
    classpath = ktlint
    mainClass.set("com.pinterest.ktlint.Main")
    args = listOf("src/**/*.kt")
}

val exclusion: Array<String> = arrayOf(
    "com/global/payment/commons/**",
    "com/global/payment/application/gateway/httpclient/commons/**",
    "com/global/payment/application/ApplicationKt.*",
)
tasks.withType<JacocoReport> {
    dependsOn(tasks.test)
    doLast {
        println("View code coverage at:")
        println("file://$buildDir/reports/jacoco/test/html/index.html")
    }
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.map {
            fileTree(it).apply {
                exclude(*exclusion)
            }
        }))
    }
}
tasks.withType<JacocoCoverageVerification> {
    dependsOn(tasks.jacocoTestReport)
    violationRules {
        isFailOnViolation = true
        rule {
            limit {
                counter = "INSTRUCTION"
                minimum = "0.98".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "LINE"
                minimum = "0.99".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "BRANCH"
                minimum = "0.93".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "METHOD"
                minimum = "0.98".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "CLASS"
                minimum = "1.00".toBigDecimal()
            }
        }
    }
    afterEvaluate {
        classDirectories.setFrom(files(classDirectories.files.map {
            fileTree(it).apply {
                exclude(*exclusion)
            }
        }))
    }
}

tasks {
    graalvmNative {
        binaries {
            named("main") {
                imageName.set("ces-micronaut-example")
                buildArgs.add("--verbose")
                buildArgs.add("--initialize-at-run-time=com.fasterxml.jackson.module.kotlin")
            }
        }
    }
}