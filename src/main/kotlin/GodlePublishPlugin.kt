package io.github.frontrider.godle.publish

import io.github.frontrider.godle.publish.dsl.AddonArtifact
import io.github.frontrider.godle.publish.dsl.PublishingExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

class GodlePublishPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        val objects = target.objects

        val addonArtifacts = objects.domainObjectContainer(AddonArtifact::class.java) { name ->
            objects.newInstance(AddonArtifact::class.java, name)
        }

        target.extensions.add("godlePublish", addonArtifacts)

        val publishTask = target.tasks.create("godleAddonPublish") {
            with(it) {
                group = "publishing"
                description = "publishes all registered addons."
            }
        }

        addonArtifacts.all {
            val addonPublish = target.tasks.register("publishAddon${it.name.replaceFirstChar { it.uppercase() }.replace(" ","")}", GodotAddonPublish::class.java, it)
            publishTask.dependsOn(addonPublish)
        }

    }
}