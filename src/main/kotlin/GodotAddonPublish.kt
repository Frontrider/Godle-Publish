package io.github.frontrider.godle.publish

import godot.assets.api.AssetsEditApi
import godot.assets.api.AuthApi
import godot.assets.model.AuthenticatedAssetDetails
import godot.assets.model.UsernamePassword
import io.github.frontrider.godle.publish.dsl.AddonArtifact
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import javax.inject.Inject

/**
 * Publishes the project's addon to the store.
 * */
open class GodotAddonPublish @Inject constructor(@Internal val artifact: AddonArtifact) : DefaultTask() {

    init {
        description = "publish addon ${artifact.name}"
        group = "publishing"
    }

    @TaskAction
    fun publish() {

        val credentials = artifact.credentials

        if (credentials.username.isEmpty()) {
            error("Godot publishing username is empty, please set it. (DO NOT write it into the buildscript, store it externally!)")
        }
        if (credentials.password.isEmpty()) {
            error("Godot publishing password is empty, please set it. (DO NOT write it into the buildscript, store it externally!)")
        }

        val authApi = AuthApi()

        val login = authApi.loginPost(UsernamePassword().apply {
            username = credentials.username
            password = credentials.password
        })


        if (login.token == null) {
            error("Failed to authenticate with the Godot Asset Library, server did not return a token. Check your credentials, and try again!")
        }

        val assetsApi = AssetsEditApi()
        val assetDetails = AuthenticatedAssetDetails().apply {
            if (artifact.id.isPresent) {
                assetId = artifact.id.get()
            }
            categoryId = "Tools"
            category = "5"

            description = artifact.description
            cost = artifact.license.licenseId

            if (artifact.downloadURL.isPresent) {
                downloadUrl = artifact.downloadURL.get()
            } else {
                println("You have not set a download URL. Be sure that the selected commit (current by default) was pushed to your remote!")
                //download commit is always available
                downloadCommit = artifact.downloadCommit.get()
            }

            downloadProvider = "GitHub"
            type = artifact.type.typeName
            version = artifact.versionString.get()
            versionString = artifact.versionString.get()
            author = login.username
            token = login.token!!
            description = artifact.description
            browseUrl = artifact.vcsUrl.get()
            issuesUrl = artifact.issuesUrl.get()
            iconUrl = artifact.issuesUrl.get()
            godotVersion = artifact.godotVersion.version
            supportLevel = artifact.supportLevel
            isArchived = artifact.isArchived
            title = artifact.title.get()
            iconUrl = artifact.iconUrl.get()
        }
        assetsApi.assetPost(assetDetails)
    }
}