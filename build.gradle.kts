import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.utils.addExtendsFromRelation
import com.google.gson.Gson

plugins {
    kotlin("jvm") version "1.6.21"
    `java-gradle-plugin`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.0.0"
    id("org.openapi.generator") version "6.2.1"
}

group = "io.github.frontrider"
version = "0.2.1"

allprojects {
    tasks.withType(Javadoc::class.java).all { enabled = false }
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}


val openapiDir = "$buildDir/generated"
val generatedLicenseSource = project.buildDir.absolutePath+"/license/src"
java {
    sourceSets {
        main {
            this.java.apply{
                srcDir("$openapiDir/src/main/java")
            }
        }
    }
}

kotlin {
    sourceSets {
        main {
            this.kotlin.apply{
                srcDir(generatedLicenseSource)
            }
        }
    }
}

val functionalTest: SourceSet by sourceSets.creating
val integrationTest: SourceSet by sourceSets.creating

addExtendsFromRelation("integrationTestImplementation", "testImplementation")
addExtendsFromRelation("functionalTestImplementation", "testImplementation")


val swaggerAnnotationsVersion = "1.5.22"
val jacksonVersion = "2.13.4"
val jakartaAnnotationVersion = "1.3.5"

dependencies {

    implementation(gradleApi())
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    // https://mvnrepository.com/artifact/org.apache.commons/commons-lang3
    implementation("org.apache.commons:commons-lang3:3.12.0")
    // https://mvnrepository.com/artifact/commons-io/commons-io
    implementation("commons-io:commons-io:2.11.0")
    //downloader plugin.
    implementation("fi.linuxbox.gradle:gradle-download:0.6")

    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-core
    //json parsing to interact with the godot asset store.
    implementation("com.fasterxml.jackson.core:jackson-core:2.14.0")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.14.0")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.14.0")
    // https://mvnrepository.com/artifact/com.fasterxml.jackson.core/jackson-annotations
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.14.0")

    testImplementation(gradleTestKit())
    testImplementation("org.junit.jupiter:junit-jupiter:5.9.0")
    testImplementation("org.junit.platform:junit-platform-runner:1.9.0")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.9.0")

    //corutines for async execution of tests.
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
    // https://mvnrepository.com/artifact/org.jetbrains.kotlinx/kotlinx-coroutines-core-jvm
    runtimeOnly("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4")

    //git compat, used to fetch some repository information.
    // https://mvnrepository.com/artifact/org.eclipse.jgit/org.eclipse.jgit
    implementation("org.eclipse.jgit:org.eclipse.jgit:6.4.0.202211300538-r")

    //openapi:
    implementation("io.swagger:swagger-annotations:$swaggerAnnotationsVersion")
    implementation("com.google.code.findbugs:jsr305:3.0.2")
    implementation("com.fasterxml.jackson.core:jackson-core:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-annotations:$jacksonVersion")
    implementation("com.fasterxml.jackson.core:jackson-databind:$jacksonVersion")
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jacksonVersion")
    implementation("org.openapitools:jackson-databind-nullable:0.2.4")
    implementation("jakarta.annotation:jakarta.annotation-api:$jakartaAnnotationVersion")
}

pluginBundle {
    vcsUrl = "https://github.com/Frontrider/GodlePublish"
    website = vcsUrl

    tags = listOf("game development", "godot")
}

gradlePlugin {
    isAutomatedPublishing = false

    plugins {
        create("godlePublish") {
            id = "io.github.frontrider.godle-publish"
            displayName = "Godle Publish"
            implementationClass = "io.github.frontrider.godle.publish.GodlePublishPlugin"
            description = "Publish Godot addons with Gradle."
        }
    }
    testSourceSets(functionalTest)
    testSourceSets(integrationTest)
}

publishing {
    publications {
        create<MavenPublication>("pluginMaven") {
            groupId = project.group.toString()
            artifactId ="godle-publish"
            version = project.version.toString()
            from(components["java"])
        }
    }
}


openApiGenerate {
    generatorName.set("java")
    inputSpec.set("$rootDir/src/main/resources/AssetStore.yaml")
    outputDir.set(openapiDir)
    apiPackage.set("godot.assets.api")
    modelPackage.set("godot.assets.model")
    invokerPackage.set("godot.assets.invoker")

    configOptions.putAll(
        mapOf(
            "annotationLibrary" to "swagger1",
            "apiPackage" to "godot.assets.api",
            "invokerPackage" to "godot.assets.invoker",
            "modelPackage" to "godot.assets.model",
            "groupId" to "godot.assets",
            "serializationLibrary" to "jackson",
            "performBeanValidation" to "false",
            "artifactId" to "godot",
            "library" to "native",
            "ensureUniqueParams" to "true",
            "snapshotVersion" to "false",
            "dateLibrary" to "legacy",
            "sortParamsByRequiredFlag" to "true",
            "sortModelPropertiesByRequiredFlag" to "true",
            "useSingleRequestParameter" to "false"
        )
    )
}

val compileKotlin: Task by tasks

//generate license information.
class LicenseData(
    var licenseListVersion: String? = null,
    var licenses: ArrayList<License> = arrayListOf(),
    var releaseDate: String? = null
)

class License(
    var reference: String?=null,
    var isDeprecatedLicenseId: Boolean?=null,
    var detailsUrl: String?=null,
    var referenceNumber: Int?=null,
    var name: String?=null,
    var licenseId: String?=null,
    var seeAlso: List<String> = emptyList(),
    var isOsiApproved: Boolean?=null
)

val generateLicenseData= tasks.create("generateLicenseData") {
    compileKotlin.dependsOn(this)
    doFirst {
        val text = File(project.rootDir, "/src/main/resources/licenses.json").readText()
        val licenseData = Gson().fromJson(text, LicenseData::class.java)
        file("$generatedLicenseSource/License.kt").apply {
            parentFile.mkdirs()
            writeText(
                """
            package godle.license
            
            class License(
                var reference: String,
                var isDeprecatedLicenseId: Boolean,
                var detailsUrl: String,
                var referenceNumber: Int,
                var name: String,
                var licenseId: String,
                var seeAlso: List<String>,
                var isOsiApproved: Boolean
            )
        """.trimIndent()
            )
        }
        licenseData.licenses.forEach {
            val fileName = it.licenseId!!.replace(".","_")
            file("$generatedLicenseSource/${fileName}.kt").apply {
                writeText(
                    """
            package godle.license
            import io.github.frontrider.godle.publish.dsl.AddonArtifact
            
            fun AddonArtifact.`${fileName}`():License{
                return License(
                    reference = "${it.reference}",
                    referenceNumber = ${it.referenceNumber},
                    isDeprecatedLicenseId = ${it.isDeprecatedLicenseId},
                    name = ${"\"\"\""+it.name+"\"\"\""},
                    licenseId = "${it.licenseId}",
                    isOsiApproved = ${it.isOsiApproved},
                    detailsUrl= "${it.detailsUrl}",
                    seeAlso = listOf(
                        ${it.seeAlso.map { also ->
                            """"$also""""
                        }.joinToString(",")}
                    )
                )
            }
        """.trimIndent()
                )
            }
        }
    }
}

compileKotlin.dependsOn("openApiGenerate")
