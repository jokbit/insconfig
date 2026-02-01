package org.jokbit.InsConfig.helper;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.loading.FMLPaths;
import org.jokbit.InsConfig.Config;
import org.jokbit.InsConfig.common.R;
import org.slf4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;

public class InsConfigHelper {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String INS_CONFIG = "insconfig";

    public static final Path INS_CONFIG_DIR = FMLPaths.GAMEDIR.get().resolve(INS_CONFIG);

    public static R insconfig(String group) {
        LOGGER.info("overrideConfig start group: {}===============================", group);
        List<Runnable> taskList = new ArrayList<>();
        try {
            String relativePath = Config.getConfigMap().get(group);
            if (Objects.isNull(relativePath)) {
                LOGGER.warn("group is null: {}", group);
                return R.builder()
                        .code(R.WARN)
                        .message("override failed, the group " + group + " is not exist")
                        .build();
            }
            Path insConfigGroup = INS_CONFIG_DIR.resolve(relativePath);
            List<Path> insConfigList = Files.walk(insConfigGroup)
                    .filter(Files::isRegularFile)
                    .toList();
            for (Path insConfig : insConfigList) {
                Path originConfig = FMLPaths.CONFIGDIR.get().resolve(insConfigGroup.relativize(insConfig));
                if (!Files.exists(originConfig) || !Files.isRegularFile(insConfig)) {
                    LOGGER.warn("file not exist or regular file");
                    continue;
                }

                Runnable task = () -> {
                    try {
                        Files.copy(insConfig, originConfig, StandardCopyOption.REPLACE_EXISTING);
                        LOGGER.info("insConfig: {}", insConfig);
                        LOGGER.info("originConfig:{}", originConfig);
                        Thread.sleep(10);
                        FileTime newTime = FileTime.from(Instant.now());
                        Files.setLastModifiedTime(originConfig, newTime);
                    } catch (IOException e) {
                        LOGGER.error(" {}", e.getMessage());
                    } catch (InterruptedException e) {
                        LOGGER.error(e.getMessage());
                    }
                };
                taskList.add(task);
            }
        } catch (IOException e) {
            LOGGER.error("override error: {}", e.getMessage());
            return R.builder()
                    .code(R.ERROR)
                    .message("occur IOException: " + e.getMessage())
                    .build();
        }
        Executors.newSingleThreadExecutor()
                .submit(() -> {
                    for (Runnable task : taskList) {
                        task.run();
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            LOGGER.error("{}", e.getMessage());
                        }
                    }
                });

        LOGGER.info("overrideConfig end===============================");

        return R.builder()
                .code(R.OK)
                .message("override group " + group + " success!")
                .build();
    }
}
