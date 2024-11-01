pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "NavigationGeneration"
include(":app")

include(":feature:feature-a")
include(":feature:feature-b")
include(":feature:feature-c")
include(":core:navigation:feature-launcher-api")
include(":core:navigation:navigation-processor")
include(":core:navigation:graph")
include(":core:navigation:navigation-annotation")
