// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    // Upgraded to 8.3.0 to match Gradle 8.5
    id("com.android.application") version "8.3.0" apply false

    // Kotlin 1.9.22 is stable and works well here
    id("org.jetbrains.kotlin.android") version "1.9.22" apply false

    // Google Services
    id("com.google.gms.google-services") version "4.4.0" apply false
}