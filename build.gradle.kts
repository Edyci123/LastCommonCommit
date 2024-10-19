plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    implementation("com.fasterxml.jackson.core:jackson-core:2.18.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.18.0")
}

tasks.test {
    useJUnitPlatform()
}