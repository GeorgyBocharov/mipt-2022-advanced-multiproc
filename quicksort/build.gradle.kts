plugins {
    kotlin("kapt") apply true
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    kapt("org.openjdk.jmh:jmh-generator-annprocess:1.35")
    implementation("org.openjdk.jmh:jmh-core:1.35")
    implementation("org.openjdk.jmh:jmh-generator-annprocess:1.21")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.13.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.9.0")
    testImplementation("org.assertj:assertj-core:3.23.1")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.9.0")
}


sourceSets.create("jmh") {
    java.setSrcDirs(listOf("src/main/kotlin"))
}


tasks.test {
    useJUnitPlatform()
}

tasks {
    register("jmh", type=JavaExec::class) {
        dependsOn("jmhClasses", "compileJava")
        group = "benchmark"
        main = "org.openjdk.jmh.Main"
        classpath = sourceSets["jmh"].runtimeClasspath
        // To pass parameters ("-h" gives a list of possible parameters)
        // args(listOf("-h"))
    }
}

