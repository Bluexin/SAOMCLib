## SAOMCLib

Minecraft forge mod library that handles common code regarding Capabilities, networking (trough packets), and much more.
It's currently main player-oriented feature is a complete party system implementation.

To add this library to your dev workspace, add the following to your build.gradle file (not inside the `buildscript` block!) :

```groovy
repositories {
    maven {
        url = "http://maven.bluexin.be/repository/releases/"
    }
}

dependencies {
    compile("be.bluexin:saomc-lib:$saomclibversion")
}
```

The library's version comes in the form `$mcversion-$libversion`.