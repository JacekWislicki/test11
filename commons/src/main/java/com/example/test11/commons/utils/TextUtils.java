package com.example.test11.commons.utils;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TextUtils {

    public static String enumToString(Enum<?> value) {
        return value != null ? value.name() : null;
    }

    public static String charSequenceToString(CharSequence value, boolean trim) {
        return value != null ? trimValue(value.toString(), trim) : null;
    }

    private static String trimValue(String value, boolean trim) {
        return trim ? value.trim() : value;
    }
}
