plugins {
    id("org.jetbrains.kotlin.jvm")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {
    jvmToolchain(17)
}

dependencies {
    implementation(project(":shared"))
    implementation(compose.desktop.currentOs)
}

compose.desktop {
    application {
        mainClass = "com.botillon.blocksmith.MainKt"
    }
}

tasks.register<JavaExec>("gallery") {
    group = "verification"
    description = "Rend les écrans du jeu en PNG pour la revue visuelle."
    mainClass.set("com.botillon.blocksmith.GalleryKt")
    classpath = sourceSets["main"].runtimeClasspath
    jvmArgs("-Dskiko.renderApi=SOFTWARE", "-Djava.awt.headless=true")
}

tasks.register<JavaExec>("tune") {
    group = "verification"
    description = "Rejoue les 48 niveaux avec le bot pour vérifier l'équilibrage."
    mainClass.set("com.botillon.blocksmith.TunerKt")
    classpath = sourceSets["main"].runtimeClasspath
}
