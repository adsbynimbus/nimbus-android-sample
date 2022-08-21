plugins {
    `kotlin-dsl`
}

java.targetCompatibility = JavaVersion.VERSION_11.also {
    kotlinDslPluginOptions.jvmTarget.set(it.majorVersion)
}

dependencies {
    implementation("com.android.tools.build:gradle:7.2.2")
    implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.7.10")
}
