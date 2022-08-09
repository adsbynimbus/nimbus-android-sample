plugins {
    base // Base Gradle plugin which adds the build and clean tasks
    id("com.android.application") apply(false)
    id("com.android.library") apply(false)
    kotlin("android") apply(false)
}

