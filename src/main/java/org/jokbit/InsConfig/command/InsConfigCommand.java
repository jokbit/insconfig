package org.jokbit.InsConfig.command;

import com.google.common.collect.Sets;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.logging.LogUtils;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jokbit.InsConfig.Config;
import org.jokbit.InsConfig.InsConfig;
import org.jokbit.InsConfig.helper.I18n;
import org.jokbit.InsConfig.helper.InsConfigHelper;
import org.slf4j.Logger;

import java.nio.file.Path;
import java.util.Set;

import static org.jokbit.InsConfig.helper.I18n.t;


@Mod.EventBusSubscriber(modid = InsConfig.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InsConfigCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String INS_CONFIG = "insconfig";

    private static final String MIRROR = "mirror";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(INS_CONFIG)
                .then(Commands.argument(MIRROR, StringArgumentType.string())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Config.getConfigMap().keySet(),
                                builder
                        ))
                        .executes(context -> execute(
                                context.getSource(),
                                StringArgumentType.getString(context, MIRROR)
                        ))
                )
        );
    }

    private static int execute(CommandSourceStack source, String mirror) {
        source.sendSystemMessage(Component.literal("insconfig: " + mirror + "..."));
        LOGGER.info("executor command insconfig mirror: {}", mirror);
        InsConfigHelper.insconfig(mirror, (successSet) -> {
            Set<Path> insConfigSet = InsConfigHelper.getInsConfigSet(mirror);
            String message;
            if (insConfigSet.size() == successSet.size()) {
                message = t(I18n.SUCC_TO_CONFIG).formatted(insConfigSet.size());
            } else {
                Sets.SetView<Path> difference = Sets.difference(insConfigSet, successSet);
                message = t(I18n.FAIL_TO_CONFIG).formatted(difference);
            }
            source.sendSystemMessage(Component.literal(message));
        });

        return Command.SINGLE_SUCCESS;
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        InsConfigCommand.register(event.getDispatcher());
    }
}