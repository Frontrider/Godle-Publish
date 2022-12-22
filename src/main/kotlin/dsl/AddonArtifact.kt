package io.github.frontrider.godle.publish.dsl

import godle.license.License
import godle.license.MIT
import godot.assets.model.AuthenticatedAssetDetails
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.internal.storage.file.FileRepository
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.RevCommit
import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.internal.provider.Providers
import org.gradle.api.model.ObjectFactory
import java.io.File
import java.io.IOException
import javax.inject.Inject


@Throws(IOException::class)
internal fun openOrCreate(gitDirectory: File): Git {
    val repository: Repository = FileRepository(gitDirectory)
    try {
        repository.create()
    } catch (repositoryExists: IllegalStateException) {
    }
    return Git(repository)
}

fun AddonArtifact.currentCommitHash(): String {
    val git = openOrCreate(File(project.rootDir,"/.git"))
    val latestCommit: RevCommit = git.log().setMaxCount(1).call().iterator().next()
    return latestCommit.name
}

open class AddonArtifact @Inject constructor(val name :String,objectFactory: ObjectFactory, val project: Project){

    //IF set then the plugin will try to update an existing asset(?)
    var id = objectFactory.property(String::class.java)

    var vcsUrl = objectFactory.property(String::class.java)
    var category = AssetCategories.Tools
    var type = AddonType.ADDON

    var root = objectFactory.directoryProperty().set(project.rootDir)
    //the download commit by default is always set to the last commit.
    var downloadCommit = objectFactory.property(String::class.java).convention(Providers.changing {
        currentCommitHash()
    })
    var description:String =""
    //if download url is set, then we don't use the commit hash.
    var downloadURL = objectFactory.property(String::class.java)
    var license: License = MIT()
    var godotVersion = CompatVersion.`3_5`
    var iconUrl = objectFactory.property(String::class.java)

    var issuesUrl = objectFactory.property(String::class.java).convention(Providers.changing {
        "$vcsUrl/issues"
    })
    var supportLevel: AuthenticatedAssetDetails.SupportLevelEnum = AuthenticatedAssetDetails.SupportLevelEnum.TESTING
    var title = objectFactory.property(String::class.java).convention(Providers.changing {
        project.name
    })
    var versionString = objectFactory.property(String::class.java).convention(Providers.changing {
        project.version.toString()
    })
    var isArchived = false
    val credentials = Credentials()
    fun credentials(action: Action<in Credentials>) {
        action.execute(credentials)
    }
}