package ch.ifocusit.plantuml.utils;

import ch.ifocusit.plantuml.classdiagram.model.clazz.JavaClazz;
import org.apache.commons.lang3.StringUtils;

public class PlantUmlUtils {

    private static boolean isHide(JavaClazz javaClazz, String part, String type) {
        return StringUtils.isNotBlank(part) && part.contains("hide " + type) && !part.contains("show " + javaClazz.getName() + " " + type);
    }

    public static boolean hideFields(JavaClazz javaClazz, String part) {
        return isHide(javaClazz, part, "fields");
    }

    public static boolean hideMethods(JavaClazz javaClazz, String part) {
        return isHide(javaClazz, part, "methods");
    }


}
