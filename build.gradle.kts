plugins {
    java
    id("com.github.johnrengelman.shadow") version "7.1.2"
    application
}

group = "dev.blucobalt"

repositories {
    mavenCentral()
}

version = "2.0.0"

dependencies {
    implementation("org.jetbrains:annotations:23.0.0")
    implementation("com.google.code.gson:gson:2.10")
    implementation("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")
    implementation("org.apache.logging.log4j:log4j-api:2.19.0")
    implementation("org.apache.logging.log4j:log4j-core:2.19.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine")
    compileOnly("systems.manifold:manifold-rt:2022.1.29")
    // Add manifold to -processorpath for javac
    annotationProcessor("systems.manifold:manifold-strings:2022.1.29")
    testAnnotationProcessor("systems.manifold:manifold-strings:2022.1.29")

}

tasks.getByName<Test>("test") {
    useJUnitPlatform()
}

tasks.compileJava {
    options.release.set(17)
    options.compilerArgs.add("-Xplugin:Manifold")
}

val jar by tasks.getting(Jar::class) {
    manifest {
        attributes["Multi-Release"] = "true"
    }
}

application {
    mainClass.set("dev.blucobalt.Entrypoint")
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
//        implementation.set(JvmImplementation.)
    }
}