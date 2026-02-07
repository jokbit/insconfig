package org.jokbit.InsConfig.helper;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.toml.TomlFormat;
import com.mojang.logging.LogUtils;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.config.ConfigTracker;
import net.minecraftforge.fml.config.IConfigEvent;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jokbit.InsConfig.Config;
import org.jokbit.InsConfig.core.ConfigFileType;
import org.jokbit.InsConfig.ex.InvocationException;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InsConfigHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String INS_CONFIG = "insconfig";

    public static final Path INS_CONFIG_DIR = FMLPaths.GAMEDIR.get().resolve(INS_CONFIG);

    public static void insconfig(String mirror, Consumer<Set<Path>> then) {
        LOGGER.info("insconfig mirror:{}", mirror);
        String mirrorPath = Config.getConfigMap().get(mirror);
        if (mirrorPath == null) {
            then.accept(Set.of());
            return;
        }
        Set<Path> insConfigSet = getInsConfigSet(mirror);
        Iterator<Path> it = insConfigSet.iterator();
        Set<Path> successSet = new HashSet<>();
        ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleWithFixedDelay(() -> {
            if (it.hasNext()) {
                Path path = it.next();
                boolean res;
                try {
                    Path insconfigPath = INS_CONFIG_DIR.resolve(mirrorPath).resolve(path);
                    Path originConfigPath = FMLPaths.CONFIGDIR.get().resolve(path);
                    if (path.toString().endsWith(ConfigFileType.SUFFIX_TOML)) {
                        res = insconfigToml(insconfigPath, originConfigPath);
                    } else {
                        res = insconfigOther(insconfigPath, originConfigPath);
                    }
                    if (res) {
                        successSet.add(path);
                    }
                } catch (Exception e) {
                    LOGGER.error("insconfig err: {}, path: {}, strace: {}", e.getMessage(), path, e.getStackTrace());
                }
            } else {
                service.shutdown();
                then.accept(Set.copyOf(successSet));
            }
        }, 0L, 50L, TimeUnit.MILLISECONDS);
    }

    public static boolean insconfigToml(Path insconfigPath, Path originConfigPath) {
        if (Files.notExists(insconfigPath) | Files.notExists(originConfigPath)) {
            return false;
        }
        ModConfig modConfig = ConfigTracker.INSTANCE
                .fileMap()
                .get(insconfigPath.getFileName().toString());
        if (modConfig != null) {
            return insconfigStandardToml(insconfigPath, modConfig);
        } else {
            return insconfigCustomToml(insconfigPath, originConfigPath);
        }
    }

    public static boolean insconfigStandardToml(Path insconfigPath, ModConfig modConfig) {
        if (Files.notExists(insconfigPath) || modConfig == null) {
            return false;
        }
        try {
            CommentedConfig insconfig = TomlFormat.instance()
                    .createParser()
                    .parse(Files.readString(insconfigPath));
            CommentedConfig originConfig = TomlFormat.instance()
                    .createParser()
                    .parse(Files.readString(modConfig.getFullPath()));

            for (CommentedConfig.Entry entry : insconfig.entrySet()) {
                originConfig.set(entry.getKey(), entry.getValue());
            }

            if (modConfig.getSpec() instanceof ForgeConfigSpec spec) {
                LOGGER.info("insconfig ForgeConfigSpec");
                spec.setConfig(originConfig);
                spec.save();
            } else {
                LOGGER.info("insconfig spec");
                modConfig.getSpec().acceptConfig(originConfig);
            }

            modConfig.getConfigData().putAll(originConfig);
            fireReloadEvent(modConfig);
            LOGGER.info("insconfigStandardToml path: {}", insconfigPath);
            return true;
        } catch (Exception e) {
            LOGGER.error("insconfigStandardToml err: {}, strace: {}", e.getMessage(), e.getStackTrace());
            return false;
        }
    }

    public static boolean insconfigCustomToml(Path insconfigPath, Path originConfigPath) {
        if (Files.notExists(insconfigPath) || Files.notExists(originConfigPath)) {
            return false;
        }
        CommentedFileConfig originConfig = null;
        try {
            originConfig = CommentedFileConfig.builder(originConfigPath)
                    .sync()
                    .concurrent()
                    .preserveInsertionOrder()
                    .build();
            CommentedConfig insconfig = TomlFormat.instance()
                    .createParser()
                    .parse(Files.readString(insconfigPath));
            originConfig.load();
            for (String key : insconfig.valueMap().keySet()) {
                Object value = insconfig.valueMap().get(key);
                originConfig.set(key, value);
            }
            originConfig.save();
            insertModifyDescription(originConfigPath);
            Thread.sleep(10L);
            Files.setLastModifiedTime(originConfigPath, FileTime.from(Instant.now()));
            LOGGER.info("insconfigCustomToml path: {}", insconfigPath);
            return true;
        } catch (Exception e) {
            LOGGER.error("insconfigCustomToml err: {}, strace: {}", e.getMessage(), e.getStackTrace());
            return false;
        } finally {
            if (originConfig != null) {
                originConfig.close();
            }
        }
    }

    public static boolean insconfigOther(Path insconfigPath, Path originConfigPath) {
        try {
            Files.copy(insconfigPath, originConfigPath, StandardCopyOption.REPLACE_EXISTING);
            Thread.sleep(10L);
            Files.setLastModifiedTime(originConfigPath, FileTime.from(Instant.now()));
            return true;
        } catch (Exception e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }

    public static Set<Path> getInsConfigSet(String mirror) {
        String mirrorPath = Config.getConfigMap().get(mirror);
        return traverseRegularFiles(INS_CONFIG_DIR.resolve(mirrorPath));
    }

    public static Set<Path> traverseRegularFiles(Path basePath) {
        try (Stream<Path> pathStream = Files.walk(basePath)) {
            return pathStream
                    .filter(Files::isRegularFile)
                    .map(basePath::relativize)
                    .collect(Collectors.toUnmodifiableSet());
        } catch (Exception e) {
            LOGGER.error("traverseRegularFiles err: {}, strace: {}", e.getMessage(), e.getStackTrace());
            return Set.of();
        }
    }

    public static void fireReloadEvent(ModConfig modConfig) {
        try {
            ReflectUtil.invoke(modConfig, "fireEvent", new Class[]{IConfigEvent.class}, IConfigEvent.reloading(modConfig));
        } catch (InvocationException e) {
            LOGGER.error("fireReloadEvent err: {}, strace: {}", e.getMessage(), e.getStackTrace());
        }
    }


    public static void insertModifyDescription(Path path) {
        try {
            Files.writeString(path, "\n# modify by insconfig " + Instant.now(),
                    StandardOpenOption.APPEND,
                    StandardOpenOption.WRITE,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            LOGGER.error("appendModifyDescription err: {}", path);
        }
    }
}
