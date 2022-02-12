package com.blakebr0.mysticalcustomization.create;

import com.blakebr0.mysticalagriculture.api.MysticalAgricultureAPI;
import com.blakebr0.mysticalagriculture.api.crop.Crop;
import com.blakebr0.mysticalagriculture.api.crop.CropTextures;
import com.blakebr0.mysticalagriculture.api.lib.LazyIngredient;
import com.blakebr0.mysticalcustomization.MysticalCustomization;
import com.blakebr0.mysticalcustomization.loader.CropLoader;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class CropCreator {
    public static Crop create(ResourceLocation id, JsonObject json) throws JsonSyntaxException {
        var tierId = GsonHelper.getAsString(json, "tier");
        var typeId = GsonHelper.getAsString(json, "type");

        var tier = MysticalAgricultureAPI.getCropRegistry().getTierById(new ResourceLocation(tierId));
        if (tier == null)
            throw new JsonSyntaxException("Invalid crop tier provided: " + tierId);

        var type = MysticalAgricultureAPI.getCropRegistry().getTypeById(new ResourceLocation(typeId));
        if (type == null)
            throw new JsonSyntaxException("Invalid crop type provided: " + typeId);

        var ingredient = json.has("ingredient") ? GsonHelper.getAsJsonObject(json, "ingredient") : null;
        var material = LazyIngredient.EMPTY;

        if (ingredient != null) {
            if (ingredient.has("tag")) {
                var tag = GsonHelper.getAsString(ingredient, "tag");
                material = LazyIngredient.tag(tag);
            } else if (ingredient.has("item")) {
                var item = GsonHelper.getAsString(ingredient, "item");
                if (ingredient.has("nbt")) {
                    var nbt = ParsingUtils.parseNBT(ingredient.get("nbt"));
                    material = LazyIngredient.item(item, nbt);
                } else {
                    material = LazyIngredient.item(item);
                }
            } else {
                throw new JsonSyntaxException("Ingredient must have either 'item' or 'tag' property");
            }
        }

        var crop = new Crop(id, tier, type, material);

        if (json.has("color")) {
            var color = GsonHelper.getAsString(json, "color");
            var i = ParsingUtils.parseHex(color, "color");
            crop.setColor(i);
        } else if (json.has("colors")) {
            var colors = GsonHelper.getAsJsonObject(json, "colors");
            if (colors.has("flower")) {
                var color = GsonHelper.getAsString(colors, "flower");
                var i = ParsingUtils.parseHex(color, "flower");

                crop.setFlowerColor(i);
            }

            if (colors.has("essence")) {
                var color = GsonHelper.getAsString(colors, "essence");
                var i = ParsingUtils.parseHex(color, "essence");

                crop.setEssenceColor(i);
            }

            if (colors.has("seeds")) {
                var color = GsonHelper.getAsString(colors, "seeds");
                var i = ParsingUtils.parseHex(color, "seeds");

                crop.setSeedColor(i);
            }
        }

        CropTextures ctextures = crop.getTextures()
                .setFlowerTexture(CropTextures.FLOWER_INGOT_BLANK)
                .setEssenceTexture(CropTextures.ESSENCE_INGOT_BLANK)
                .setSeedTexture(CropTextures.SEED_BLANK);

        if (json.has("textures")) {
            var textures = GsonHelper.getAsJsonObject(json, "textures");
            if (textures.has("flower")) {
                var texture = GsonHelper.getAsString(textures, "flower");
                var location = new ResourceLocation(texture);

                ctextures.setFlowerTexture(location);
            }

            if (textures.has("essence")) {
                var texture = GsonHelper.getAsString(textures, "essence");
                var location = new ResourceLocation(texture);

                ctextures.setEssenceTexture(location);
            }

            if (textures.has("seeds")) {
                var texture = GsonHelper.getAsString(textures, "seeds");
                var location = new ResourceLocation(texture);

                ctextures.setSeedTexture(location);
            }
        }

        if (json.has("name")) {
            var name = GsonHelper.getAsString(json, "name");
            crop.setDisplayName(new TextComponent(name));
        }

        if (json.has("baseSecondaryChance")) {
            var chance = GsonHelper.getAsDouble(json, "baseSecondaryChance");
            crop.setBaseSecondaryChance(chance);
        }

        if (json.has("enabled")) {
            var enabled = GsonHelper.getAsBoolean(json, "enabled");
            crop.setEnabled(enabled);
        }

        if (json.has("crux")) {
            var crux = GsonHelper.getAsString(json, "crux");
            CropLoader.CRUX_MAP.put(crop, new ResourceLocation(crux));
        }

        if (json.has("glint")) {
            var glint = GsonHelper.getAsBoolean(json, "glint");
            crop.setHasEffect(glint);
        }

        if (json.has("biomes")) {
            var biomes = GsonHelper.getAsJsonArray(json, "biomes");

            biomes.forEach(biome -> {
                crop.addRequiredBiome(new ResourceLocation(biome.getAsString()));
            });
        }

        if (isGarbageSeed(crop.getName())) {
            var essence = RegistryObject.of(new ResourceLocation(MysticalAgricultureAPI.MOD_ID, crop.getNameWithSuffix("essence")), ForgeRegistries.ITEMS);

            essence.updateReference(ForgeRegistries.ITEMS);

            crop.setEssenceItem(essence);

            if (!essence.isPresent()) {
                MysticalCustomization.LOGGER.error("Could not find the essence for crop {}", crop.getId());
            }
        }

        return crop;
    }

    private static boolean isGarbageSeed(String name) {
        return "prudentium".equals(name)
                || "tertium".equals(name)
                || "imperium".equals(name)
                || "supremium".equals(name)
                || "fertilized".equals(name);
    }
}
