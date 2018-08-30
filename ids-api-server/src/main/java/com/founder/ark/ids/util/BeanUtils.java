package com.founder.ark.ids.util;

import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Set;

public class BeanUtils {
    public static final <T> T toLowerCase(T obj, String... fieldNames) {
        if (obj == null) {
            return null;
        }
        for (String fieldName :
                fieldNames) {
            try {
                Field field = obj.getClass().getDeclaredField(fieldName);
                Object o = field.get(obj);
                if (o instanceof String) {
                    field.set(obj, ((String) o).toLowerCase());
                }
            } catch (NoSuchFieldException | IllegalAccessException e) {
                LoggerFactory.getLogger(BeanUtils.class).warn(e.getLocalizedMessage());
            }
        }
        return obj;
    }

    public static String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> emptyNames = new HashSet<String>();
        for (java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }

    public static void copyPropertiesIgnoreNull(Object src, Object target) {
        org.springframework.beans.BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }
}
