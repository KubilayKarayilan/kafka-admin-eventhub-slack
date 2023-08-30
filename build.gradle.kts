plugins {
    id("java")
    id("idea")
    id("org.springframework.boot") version "3.1.3"
}
apply(plugin = "io.spring.dependency-management")
repositories {
    mavenCentral()
}
dependencies {

    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.kafka:spring-kafka")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude("junit")
    }
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.2")
    testImplementation("org.junit.vintage:junit-vintage-engine:5.9.2")
    testImplementation("org.springframework.restdocs:spring-restdocs-mockmvc")
}

