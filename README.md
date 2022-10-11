# Mystical Customization [![](http://cf.way2muchnoise.eu/full_280441_downloads.svg)](https://minecraft.curseforge.com/projects/mystical-customization)
Allows modpack creators to add new content and modify existing content in Mystical Agriculture.

[Documentation](https://blakesmods.com/docs/mysticalcustomization)

## Download

The official release builds can be downloaded from the following websites.

- [Blake's Mods](https://blakesmods.com/mystical-customization/download)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mystical-customization)

## Development

To use this mod in a development environment, you will need to add the following to your `build.gradle`.

```groovy
repositories {
    maven {
        url 'https://maven.blakesmods.com'
    }
}

dependencies {
    implementation fg.deobf('com.blakebr0.cucumber:Cucumber:<minecraft_version>-<mod_version>')
    implementation fg.deobf('com.blakebr0.mysticalcustomization:MysticalCustomization:<minecraft_version>-<mod_version>')
}
```

## License

[MIT License](./LICENSE)
