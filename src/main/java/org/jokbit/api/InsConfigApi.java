package org.jokbit.api;

import com.mojang.logging.LogUtils;
import org.jokbit.InsConfig.helper.InsConfigHelper;
import org.slf4j.Logger;

public class InsConfigApi {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static void insconfig(String group) {
        LOGGER.info("InsConfigApi insconfig group: {}", group);
        InsConfigHelper.insconfig(group);
    }
}
