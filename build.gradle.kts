import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("org.springframework.boot") version "3.0.1"
    id("org.flywaydb.flyway") version "9.4.0"
    id("io.spring.dependency-management") version "1.1.0"
    id("io.gitlab.arturbosch.detekt") version "1.21.0"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.7.22"
    id("com.github.johnrengelman.shadow") version "7.1.2"
    id("org.graalvm.buildtools.native") version "0.9.18"
    kotlin("kapt") version "1.7.22"
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"
    jacoco
}

group = "com.global.payment"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
    mavenCentral()
}

val kotlinLoggingVersion = "3.0.4"
val testcontainersVersion = "1.17.6"
val resilience4jVersion = "2.0.2"
val coroutinesVersion = "1.6.4"
val jacksonVersion = "2.13.4"
val wiremockVersion = "2.35.0"
val mockkVersion = "1.13.2"
val openApiVersion = "1.6.14"
val logstashLogbackVersion = "7.2"
val ktlint by configurations.creating

dependencies {
    runtimeOnly("org.springframework.boot:spring-boot-properties-migrator")

    kapt("org.springframework.boot:spring-boot-configuration-processor")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus:1.10.2")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")

    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("io.micrometer:micrometer-tracing-bridge-brave")
//    implementation("org.springframework.cloud:spring-cloud-starter-sleuth:3.1.5")
    implementation("io.github.microutils:kotlin-logging-jvm:$kotlinLoggingVersion")
    implementation("io.github.microutils:kotlin-logging:$kotlinLoggingVersion")

    implementation("io.github.resilience4j:resilience4j-kotlin:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-retry:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-circuitbreaker:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-bulkhead:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-micrometer:$resilience4jVersion")
    implementation("io.github.resilience4j:resilience4j-spring-boot3:$resilience4jVersion")

    implementation("jakarta.annotation:jakarta.annotation-api")
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    compileOnly("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")
    implementation("io.netty:netty-handler:4.1.86.Final")
//    implementation("org.slf4j:slf4j-api")
    implementation("org.springdoc:springdoc-openapi-kotlin:$openApiVersion")
    implementation("org.springdoc:springdoc-openapi-webflux-ui:$openApiVersion")
    implementation("org.flywaydb:flyway-core")

//    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashLogbackVersion")

    runtimeOnly("org.postgresql:r2dbc-postgresql:1.0.0.RELEASE")
    runtimeOnly("io.r2dbc:r2dbc-pool:1.0.0.RELEASE")
    runtimeOnly("io.r2dbc:r2dbc-spi:1.0.0.RELEASE")
    runtimeOnly("org.postgresql:postgresql:42.5.1")

    ktlint("com.pinterest:client:0.47.1")

    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("com.github.tomakehurst:wiremock-jre8-standalone:$wiremockVersion")
    testImplementation("org.testcontainers:junit-jupiter:$testcontainersVersion")
    testImplementation("org.testcontainers:postgresql:$testcontainersVersion")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api")
    testImplementation("io.rest-assured:rest-assured:5.3.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

//micronaut {
//    runtime("netty")
//    testRuntime("junit5")
//    processing {
//        incremental(true)
//        annotations("com.global.payment.application.*")
//    }
//}
//application {
//    mainClass.set("com.global.payment.application.ApplicationKt")
//}

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
                minimum = "0.99".toBigDecimal()
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
                minimum = "1.00".toBigDecimal()
            }
        }
        rule {
            limit {
                counter = "METHOD"
                minimum = "1.00".toBigDecimal()
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


tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "17"
    }
}
//
//tasks {
//    graalvmNative {
//        binaries {
//            named("main") {
//                imageName.set("ces-micronaut-example")
//                buildArgs.add("--verbose")
//                buildArgs.add("--initialize-at-run-time=com.fasterxml.jackson.module.kotlin")
//            }
//        }
//    }
//}