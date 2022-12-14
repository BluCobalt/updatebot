plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "dev.blucobalt"

repositories {
    mavenCentral()
}

version = "0.03"

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("com.google.code.gson:gson:2.9.0")
    implementation("org.projectlombok:lombok:1.18.24")
    implementation("org.apache.logging.log4j:log4j-api:2.18.0")
    implementation("org.apache.logging.log4j:log4j-core:2.18.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.compileJava {
    options.release.set(11)
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Multi-Release"] = "true"
    }
}

application {
    mainClass.set("dev.blucobalt.Updater")
}
