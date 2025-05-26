plugins {
	java
	jacoco
	id("org.springframework.boot") version "3.4.4"
	id("io.spring.dependency-management") version "1.1.7"
	id("org.flywaydb.flyway") version "10.20.1"  // Add Flyway plugin
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-data-rest")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("io.micrometer:micrometer-registry-prometheus")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("org.flywaydb:flyway-core")
	implementation("org.flywaydb:flyway-database-postgresql")
	implementation("com.zaxxer:HikariCP:5.1.0")
	implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("io.jsonwebtoken:jjwt-api:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")
	
    compileOnly("org.projectlombok:lombok:1.18.38")
	developmentOnly("org.springframework.boot:spring-boot-devtools")
	runtimeOnly("org.postgresql:postgresql")
	annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
}

tasks.withType<Test>() {
    useJUnitPlatform()
}

tasks.register("migrate") {
    group = "database"
    description = "Run database migrations"
    dependsOn("compileJava")
    doLast {
        project.javaexec {
            mainClass.set("com.example.paymentbe.MigrationManager")
            classpath = sourceSets["main"].runtimeClasspath
            environment.putAll(System.getenv())
        }
    }
}

// Flyway configuration
flyway {
// Use environment variables only, no fallback hardcoded values
url = System.getenv("DATABASE_URL")
user = System.getenv("DATABASE_USERNAME")
password = System.getenv("DATABASE_PASSWORD")
schemas = arrayOf("public")
locations = arrayOf("classpath:db/migration")

// Development settings
validateOnMigrate = false
cleanDisabled = false
baselineOnMigrate = true
outOfOrder = true
}