package io.github.frontrider.godle.publish.dsl


/**
 * Versions of godot that are used to mark compatibility.
 * */

enum class AddonType(val typeName:String) {
    ADDON("addon"),PROJECT("project")
}
enum class AssetCategories(val categoryName:String){
    `2DTools`("2D Tools"),
    `3DTools`("3D Tools"),
    Shaders("Shaders"),
    Materials("Materials"),
    Tools("Tools"),
    Scripts("Scripts"),
    Misc("Misc"),
    Templates("Templates"),
    Projects("Projects"),
    Demos("Demos"),
}