package org.jokbit.InsConfig.api;

import com.mojang.logging.LogUtils;
import org.jokbit.InsConfig.helper.InsConfigHelper;
import org.slf4j.Logger;

public class InsConfigApi {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void insconfig(String mirror) {
        LOGGER.info("InsConfigApi insconfig mirror: {}", mirror);
        InsConfigHelper.insconfig(mirror, (successSet) -> {});
    }
}
