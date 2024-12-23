import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import org.jetbrains.dokka.gradle.DokkaTask

plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.jetbrainsCompose) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.kotlinNativeCocoaPods) apply false
    alias(libs.plugins.dokka) apply false
    alias(libs.plugins.kotlinx.binary.validator)
    alias(libs.plugins.nexusPublish)
    id("com.google.gms.google-services") version "4.4.0" apply false
}




allprojects {
    group = "io.github.mirzemehdi"
    version = project.properties["kmpNotifierVersion"] as String

    val gpgKeySecret = gradleLocalProperties(rootDir).getProperty("gpgKeySecret")
    val gpgKeyPassword = gradleLocalProperties(rootDir).getProperty("gpgKeyPassword")

    val excludedModules = listOf(":sample")
    if (project.path in excludedModules) return@allprojects

    apply(plugin = "org.jetbrains.dokka")
    apply(plugin = "maven-publish")


    extensions.configure<PublishingExtension> {

        publications {
            withType<MavenPublication> {
                pom {
                    groupId="io.github.mirzemehdi"
                    name.set("KMPNotifier")
                    description.set(" Kotlin Multiplatform Push Notification Library targeting ios and android")
                    licenses {
                        license {
                            name.set("Apache-2.0")
                            url.set("https://opensource.org/licenses/Apache-2.0")
                        }
                    }
                    url.set("mirzemehdi.github.io/KMPNotifier/")
                    issueManagement {
                        system.set("Github")
                        url.set("https://github.com/mirzemehdi/KMPNotifier/issues")
                    }
                    scm {
                        connection.set("https://github.com/mirzemehdi/KMPNotifier.git")
                        url.set("https://github.com/mirzemehdi/KMPNotifier")
                    }
                    developers {
                        developer {
                            name.set("Mirzamehdi Karimov")
                            email.set("mirzemehdi@gmail.com")
                        }
                    }
                }
            }
        }
    }

    // TODO: remove after https://youtrack.jetbrains.com/issue/KT-46466 is fixed
    project.tasks.withType(AbstractPublishToMaven::class.java).configureEach {
        dependsOn(project.tasks.withType(Sign::class.java))
    }
}
nexusPublishing {
    repositories {
        sonatype {  //only for users registered in Sonatype after 24 Feb 2021
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
            val sonatypeUsername = gradleLocalProperties(rootDir).getProperty("sonatypeUsername")
            val sonatypePassword = gradleLocalProperties(rootDir).getProperty("sonatypePassword")
            username = sonatypeUsername
            password = sonatypePassword
        }
    }
}


