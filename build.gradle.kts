plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
}

val git: String = versionBanner()
val builder: String = builder()
ext["git_version"] = git
ext["builder"] = builder

subprojects {
    apply(plugin = "java")
    apply(plugin = "com.gradleup.shadow")

    repositories {
        mavenCentral()
    }

    tasks.processResources {
        filteringCharset = "UTF-8"

        filesMatching(listOf("custom-nameplates.properties")) {
            expand(rootProject.properties)
        }

        filesMatching(listOf("*.yml", "*/*.yml")) {
            expand(
                "project_version" to (rootProject.properties["project_version"] ?: "unknown"),
                "config_version" to (rootProject.properties["config_version"] ?: "unknown")
            )
        }
    }
}

// ✅ 修复：使用 runCatching 捕获 Git 命令执行异常，避免构建失败
fun versionBanner(): String = runCatching {
    project.providers.exec {
        commandLine("git", "rev-parse", "--short=8", "HEAD")
    }.standardOutput.asText.get().trim()
}.getOrElse { "Unknown" }

fun builder(): String = runCatching {
    project.providers.exec {
        commandLine("git", "config", "user.name")
    }.standardOutput.asText.get().trim()
}.getOrElse { "Unknown" }
