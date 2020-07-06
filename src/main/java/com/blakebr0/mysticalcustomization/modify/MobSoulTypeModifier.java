package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.soul.IMobSoulType;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.util.JSONUtils;
import net.minecraft.util.text.StringTextComponent;

public final class MobSoulTypeModifier {
    public static void modify(IMobSoulType type, JsonObject json) throws JsonSyntaxException {
        if (json.has("souls")) {
            float souls = JSONUtils.getFloat(json, "souls");
            type.setSoulRequirement(souls);
        }

        if (json.has("color")) {
            String color = JSONUtils.getString(json, "color");
            int i = ParsingUtils.parseHex(color, "color");
            type.setColor(i);
        }

        if (json.has("name")) {
            String name = JSONUtils.getString(json, "name");
            type.setEntityDisplayName(new StringTextComponent(name));
        }
    }
}
