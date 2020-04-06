package com.blakebr0.mysticalcustomization.modify;

import com.blakebr0.mysticalagriculture.api.soul.IMobSoulType;
import com.blakebr0.mysticalcustomization.util.ParsingUtils;
import com.google.gson.JsonObject;
import net.minecraft.util.JSONUtils;

public class MobSoulTypeModifier {
    public static void modify(IMobSoulType type, JsonObject json) {
        if (json.has("souls")) {
            float souls = JSONUtils.getFloat(json, "souls");
            type.setSoulRequirement(souls);
        }

        if (json.has("color")) {
            String color = JSONUtils.getString(json, "color");
            int i = ParsingUtils.parseHex(color, "color");
            type.setColor(i);
        }
    }
}
