package com.blakebr0.mysticalcustomization.create;

import com.blakebr0.mysticalagriculture.api.soul.IMobSoulType;
import com.blakebr0.mysticalagriculture.api.soul.MobSoulType;
import com.blakebr0.mysticalcustomization.loader.MobSoulTypeLoader;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.GsonHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.TextComponent;

import java.util.ArrayList;
import java.util.List;

public final class MobSoulTypeCreator {
    public static IMobSoulType create(ResourceLocation id, JsonObject json) throws JsonSyntaxException {
        float souls = GsonHelper.getAsFloat(json, "souls");

        MobSoulType type = new MobSoulType(id, Sets.newHashSet(), null, souls, -1);

        if (json.has("entity")) {
            String entityId = GsonHelper.getAsString(json, "entity");
            ResourceLocation entity = new ResourceLocation(entityId);

            MobSoulTypeLoader.ENTITY_ADDITIONS_MAP.put(type, Lists.newArrayList(entity));
        } else if (json.has("entities")) {
            JsonArray entityIds = GsonHelper.getAsJsonArray(json, "entities");
            List<ResourceLocation> entities = new ArrayList<>();

            entityIds.forEach(entity -> {
                entities.add(new ResourceLocation(entity.getAsString()));
            });

            MobSoulTypeLoader.ENTITY_ADDITIONS_MAP.put(type, entities);
        } else {
            throw new JsonSyntaxException("Missing 'entity' or 'entities' property");
        }

        if (json.has("color")) {
            String color = GsonHelper.getAsString(json, "color");
            int i = ParsingUtils.parseHex(color, "color");
            type.setColor(i);
        }

        if (json.has("name")) {
            String name = GsonHelper.getAsString(json, "name");
            type.setEntityDisplayName(new TextComponent(name));
        }

        if (json.has("enabled")) {
            boolean enabled = GsonHelper.getAsBoolean(json, "enabled");
            type.setEnabled(enabled);
        }

        return type;
    }
}
