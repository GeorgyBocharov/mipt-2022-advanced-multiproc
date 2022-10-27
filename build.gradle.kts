plugins {
    id("org.jetbrains.kotlin.jvm") version "1.5.10" apply false
}

group = "ru.sbt.multiproc"
version = "1.0-SNAPSHOT"

allprojects {
    plugins.apply("org.jetbrains.kotlin.jvm")
    println(this)

    repositories {
        mavenCentral()
    }
}