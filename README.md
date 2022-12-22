Godle addon publishing plugin.

Allows gradle to publish addons to the godot asset library. 
Highly work in progress, and experimental.

Things that don't work:
- The category can only be "tool"
- the download provider can only be "GitHub"

# Why?

Godot has no competitive tooling for addon publishing, you must go through the UI, and you can't store that configuration yourself.

This combined with [Godle](https://plugins.gradle.org/plugin/io.github.frontrider.godle), it provides a full stack for setting up everything for addon development.

# Example from one of my own addons

Store your username and password outside of the buildscript, in this case in the user's gradle.properties file.
I see that now that this exists, a lot of people will commit that user into a repository, but please don't.

```kotlin
plugins{
    id("io.github.frontrider.godle-publish") version "<plugin version>"
}

val godotUsername:String by project
val godotPassword:String by project

godlePublish{
    create("scene browser"){
        //set the id only if you have one.
        id.set("1070")
        description = """
            Lets you view scenes inside the folder "assets/components", in a list, then add them to your current scene when you need it to make level editing easier. (not limited to levels alone, but that is the primary intention of the addon)

            Also features additional post-import scripts to make it easier to work with certain model types.

            I recommend "snappy" to provide smooth vertex snapping with this to get a solid level editing experience.
            https://github.com/jgillich/godot-snappy 
        """.trimIndent()
        supportLevel = SupportLevelEnum.COMMUNITY
        godotVersion = CompatVersion.`3_5`
        vcsUrl.set("https://github.com/Frontrider/Godot-Scene-Browser/")
        iconUrl.set("https://raw.githubusercontent.com/Frontrider/Godot-Scene-Browser/${currentCommitHash()}/components.png")

        credentials{
            username = godotUsername
            password = godotPassword
        }
    }
}
```
The "legacy" plugin application seems to break the `godlePublish` block, I can't fix it.

<br/>
<br/>
<br/>
<br/>


HUGE thanks for the work of @fenix-hub for his work on creating a somewhat functional Open API specification for the godot asset library.