# Mystical Customization [![](http://cf.way2muchnoise.eu/full_280441_downloads.svg)](https://minecraft.curseforge.com/projects/mystical-customization)
Allows modpack creators to add new content and modify existing content in Mystical Agriculture.

<p align="left">
    <a href="https://blakesmods.com/mystical-customization" alt="Downloads">
        <img src="https://img.shields.io/endpoint?url=https://api.blakesmods.com/v2/badges/mysticalcustomization/downloads&style=for-the-badge" />
    </a>
    <a href="https://blakesmods.com/mystical-customization" alt="Latest Version">
        <img src="https://img.shields.io/endpoint?url=https://api.blakesmods.com/v2/badges/mysticalcustomization/version&style=for-the-badge" />
    </a>
    <a href="https://blakesmods.com/mystical-customization" alt="Minecraft Version">
        <img src="https://img.shields.io/endpoint?url=https://api.blakesmods.com/v2/badges/mysticalcustomization/mc_version&style=for-the-badge" />
    </a>
    <a href="https://blakesmods.com/docs/mysticalcustomization" alt="Docs">
        <img src="https://img.shields.io/static/v1?label=docs&message=view&color=brightgreen&style=for-the-badge" />
    </a>
</p>

## Download

The official release builds can be downloaded from the following websites.

- [Blake's Mods](https://blakesmods.com/mystical-customization/download)
- [CurseForge](https://www.curseforge.com/minecraft/mc-mods/mystical-customization)
- [Modrinth](https://modrinth.com/mod/mystical-customization)

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
