package org.jokbit.InsConfig;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// An example config class. This is not required, but it's a good idea to have one to keep your config organized.
// Demonstrates how to use Forge's config APIs
@Mod.EventBusSubscriber(modid = InsConfig.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final String REGX = "^[a-zA-Z0-9_]+\\s*:\\s*[a-zA-Z0-9_\\-]+(?:\\.[a-zA-Z0-9_\\-]+)*$";

    // a list of strings that are treated as resource locations for items
    private static final ForgeConfigSpec.ConfigValue<List<? extends String>> LIST_CONFIG = BUILDER
            .comment("A list of config mirror, example:")
            .comment("[\"mirror1:mirror_config1\", \"mirror2:phase.phase_day10.zombie_extreme\"]")
            .defineListAllowEmpty("mirrors", Collections.emptyList(), Config::validateConfigPath);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    private static boolean validateConfigPath(final Object obj) {
        return obj instanceof String && ((String) obj).matches(REGX);
    }

    public static Map<String, String> getConfigMap() {
        return LIST_CONFIG.get()
                .stream()
                .map(s -> s.split(":"))
                .filter(s -> s.length == 2)
                .map(s -> Pair.of(s[0].trim(), s[1].replace(".", File.separator).trim()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
    }

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event){

    }
}
