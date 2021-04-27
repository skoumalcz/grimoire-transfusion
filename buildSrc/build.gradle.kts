plugins {
    `kotlin-dsl`
}

kotlinDslPluginOptions {
    experimentalWarning.set(false)
}

repositories {
    google()
    jcenter()
    mavenCentral()
}


dependencies {

    compileOnly(gradleApi())

    implementation("com.android.tools.build:gradle:4.1.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.32")
    implementation("com.skoumal.grimoire:cover:1.0.0-beta03")

}

gradlePlugin {
    plugins {
        register("project.build") {
            id = "project.build"
            implementationClass = "com.skoumal.grimoire.build.LibraryPlugin"
        }
    }
}