plugins {
    `kotlin-dsl`
}

kotlin.jvmToolchain(11)

dependencies {
    implementation("com.android.tools.build:gradle:7.4.1")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.10")
}
