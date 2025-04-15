plugins {
	java
	id("org.springframework.boot") version "3.4.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(23)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Spring Boot starters
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security") // Spring Security

	// Database
	runtimeOnly("com.mysql:mysql-connector-j")

	// Security dependencies
	implementation("io.jsonwebtoken:jjwt-api:0.11.5") // JWT API
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")   // JWT Implementation
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5") // JWT Jackson support

	// Lombok
	compileOnly("org.projectlombok:lombok:1.18.30")
	annotationProcessor("org.projectlombok:lombok:1.18.30")

	// Documentation
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.5.0")

	// Email
	implementation("org.springframework.boot:spring-boot-starter-mail")

	// Testing
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test") // Security testing
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}