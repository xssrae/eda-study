plugins {
	kotlin("jvm") version "2.2.21"
	kotlin("plugin.spring") version "2.2.21"
	id("org.springframework.boot") version "4.0.4"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.xssrae"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

repositories {
	mavenCentral()
}

dependencies {
    // Spring

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.kafka:spring-kafka")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    // AWS SDK v2 — BOM gerencia versões de todos os módulos
    implementation(platform("software.amazon.awssdk:bom:2.24.0"))
    implementation("software.amazon.awssdk:s3")           // ✅ cliente S3 async
    implementation("software.amazon.awssdk:sts")          // opcional: para roles IAM

    // Kotlin
    implementation("org.jetbrains.kotlin:kotlin-reflect")

    // Coroutines — pacote correto para .await() em CompletableFuture (retorno do SDK v2)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:1.7.3")  // WebFlux
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.7.3")    // ✅ .await()

    // Testes
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.kafka:spring-kafka-test")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.7.3")
}

kotlin {
	compilerOptions {
		freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
	}
}

tasks.named<Test>("test") {
    // Pula testes que precisam de infraestrutura externa
    exclude("**/*ApplicationTests*")
}
