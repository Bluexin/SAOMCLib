## SAOMCLib

Minecraft forge mod library that handles common code regarding Capabilities, networking (trough packets), and much more.

It also enables you to use Kotlin extensively (see below).

To add this library to your dev workspace, add the following to your build.gradle file (not inside the `buildscript` block!) :

```groovy
repositories {
maven {
url = "http://maven.bluexin.be/repository/releases/"
}
}

dependencies {
compile("be.bluexin:saomclib:$saomclibversion:deobf")
}
```

The library's version comes in the form `$mcversion-$libversion`.

# Using Kotlin

SAOMCLib comes with the Kotlin libraries, currently version 1.0.5.
If your mod uses Kotlin and this lib, you won't need to shade these yourself.
All you need to do is add the following to your build.gradle file :

```groovy
reobf {
jar {
extraLines += ["PK: kotlin be/bluexin/saomclib/shade/kotlin"]
extraLines += ["PK: org/jetbrains/annotations be/bluexin/saomclib/shade/annotations"]
}
}
```
(an example can be found at [our build.gradle](build.gradle#L86))

Regarding your main mod class (annotated by @Mod), you can either make it a Kotlin class or object.
When using an object (which there is no reason not to), you need to add the following anywhere in your object's declaration :

```kotlin
@JvmStatic
@Mod.InstanceFactory
fun whatever() = this
```
(an example can be seen at [our SAOMCLib.kt](src/main/java/be/bluexin/saomclib/SAOMCLib.kt#L43))
