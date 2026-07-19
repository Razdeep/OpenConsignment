import org.gradle.api.tasks.Copy

plugins {
    application
    java
    alias(libs.plugins.javafx)
    alias(libs.plugins.spotless)
    alias(libs.plugins.shadow)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.controlsfx)
    implementation(libs.jasperreports)
    implementation(libs.sqlite)
    implementation(libs.olap4j)
//    implementation(libs.itext)

    implementation(libs.javafx.controls)
    implementation(libs.javafx.base)
    implementation(libs.javafx.graphics)
    implementation(libs.javafx.fxml)

    compileOnly(libs.lombok)
    annotationProcessor(libs.lombok)

    testImplementation(libs.testfx.junit5)
    testImplementation(libs.testfx.monocle)
    testImplementation(libs.hamcrest)

    testImplementation(platform(libs.junit.bom))
    testImplementation(libs.junit.jupiter)
    testRuntimeOnly(libs.junit.platform.launcher)

    testImplementation(libs.testfx.core)
}

javafx {
    version = libs.versions.javafx.get()
    modules = listOf("javafx.controls", "javafx.fxml", "javafx.graphics", "javafx.base")
}

tasks.test {
    useJUnitPlatform()

    if (System.getenv("CI") == "true") {
        jvmArgs(
            "-Dtestfx.robot=glass",
            "-Dtestfx.headless=true",
            "-Dprism.order=sw",
            "-Dprism.text=t2k",
            "-Djava.awt.headless=true"
        )
    }
}

spotless {
    java {
        lineEndings = com.diffplug.spotless.LineEnding.UNIX
        target("src/**/*.java")

        palantirJavaFormat()
        removeUnusedImports()
        endWithNewline()
        trimTrailingWhitespace()
    }
}

application {
    mainClass.set("openconsignment.Launcher")
}

tasks.named<JavaExec>("run") {
    jvmArgs(
        "--add-exports=javafx.base/com.sun.javafx.event=ALL-UNNAMED",
        "--add-exports=javafx.controls/com.sun.javafx.scene.control=ALL-UNNAMED",
        "--add-exports=javafx.controls/com.sun.javafx.scene.control.behavior=ALL-UNNAMED",
        "--add-exports=javafx.base/com.sun.javafx.binding=ALL-UNNAMED",
        "--add-exports=javafx.graphics/com.sun.javafx.stage=ALL-UNNAMED"
    )
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("")
}

tasks.named("distZip") { enabled = false }
tasks.named("distTar") { enabled = false }
tasks.named("startScripts") { enabled = false }
tasks.named<Jar>("jar") { enabled = false }

val copyScripts by tasks.registering(Copy::class) {
    from("scripts") {
        include("start.sh", "start.bat", "configure-backup.py", "templates/**")
    }
    into(layout.buildDirectory.dir("libs"))
    doLast {
        println("Copied scripts to build directory")
    }
}

tasks.named("shadowJar") {
    dependsOn(copyScripts)
}
