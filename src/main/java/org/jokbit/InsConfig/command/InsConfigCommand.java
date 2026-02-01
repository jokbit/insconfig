package org.jokbit.InsConfig.command;

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
import org.jokbit.InsConfig.common.R;
import org.jokbit.InsConfig.helper.InsConfigHelper;
import org.slf4j.Logger;


@Mod.EventBusSubscriber(modid = InsConfig.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class InsConfigCommand {

    private static final Logger LOGGER = LogUtils.getLogger();

    private static final String INS_CONFIG = "insconfig";

    private static final String GROUP = "group";

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(INS_CONFIG)
                .then(Commands.argument(GROUP, StringArgumentType.string())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(
                                Config.getConfigMap().keySet(),
                                builder
                        ))
                        .executes(context -> execute(
                                context.getSource(),
                                StringArgumentType.getString(context, GROUP)
                        ))
                )
        );
    }

    private static int execute(CommandSourceStack source, String group) {
        source.sendSystemMessage(Component.literal("insconfig: " + group + "..."));
        LOGGER.info("jokbit==== exec command group: {}", group);
        R r = InsConfigHelper.insconfig(group);
        source.sendSystemMessage(Component.literal(r.getMessage()));
        return Command.SINGLE_SUCCESS;
    }

    @SubscribeEvent
    public static void registerCommands(RegisterCommandsEvent event) {
        InsConfigCommand.register(event.getDispatcher());
    }
}