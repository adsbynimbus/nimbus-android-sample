plugins {
    `kotlin-dsl`
}

java.targetCompatibility = JavaVersion.VERSION_11.also {
    kotlinDslPluginOptions.jvmTarget.set(it.majorVersion)
}

dependencies {
    implementation("com.android.tools.build:gradle:7.4.0")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
}
