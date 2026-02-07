package org.jokbit.InsConfig.helper;

import com.mojang.logging.LogUtils;
import org.jokbit.InsConfig.ex.InvocationException;
import org.slf4j.Logger;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;

import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class ReflectUtil {

    private static final Logger LOGGER = LogUtils.getLogger();

    public static <T> Object invoke(T t, String methodName, Class<?>[] paramTypes, Object ...args) throws InvocationException {
        Method method = ObfuscationReflectionHelper.findMethod(t.getClass(), methodName, paramTypes);
        try {
            return method.invoke(t, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            LOGGER.error(e.getMessage());
            throw new InvocationException(e.getMessage());
        }
    }

    public static <T, K> Optional<K> getField(T t, Class<K> fileType,String fieldName) {
        Field field = ObfuscationReflectionHelper.findField(t.getClass(), fieldName);
        K k = null;
        try {
            k = fileType.cast(field.get(t));

        } catch (Exception e) {
            LOGGER.error(e.getMessage());
        }
        return Optional.ofNullable(k);
    }
}
