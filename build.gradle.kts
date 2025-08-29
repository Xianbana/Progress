// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.android.library) apply false
}

// 版本管理
ext {
    set("libraryVersion", "1.0.0")
    set("libraryGroupId", "com.github.Xianbana")
    set("libraryArtifactId", "Progress")
}