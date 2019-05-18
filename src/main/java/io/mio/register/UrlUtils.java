package io.mio.register;

import io.mio.commons.URL;

/**
 * URL 工具类
 *
 * @author lry
 */
public class UrlUtils {

    public static boolean isMatchCategory(String category, String categories) {
        if (categories == null || categories.length() == 0) {
            return Constants.DEFAULT_CATEGORY.equals(category);
        } else if (categories.contains(Constants.ANY_VALUE)) {
            return true;
        } else if (categories.contains(Constants.REMOVE_VALUE_PREFIX)) {
            return !categories.contains(Constants.REMOVE_VALUE_PREFIX + category);
        } else {
            return categories.contains(category);
        }
    }

    public static boolean isMatch(URL consumerUrl, URL providerUrl) {
        String consumerInterface = consumerUrl.getServiceInterface();
        String providerInterface = providerUrl.getServiceInterface();
        if (!(Constants.ANY_VALUE.equals(consumerInterface) || consumerInterface.equals(providerInterface))) {
            return false;
        }

        if (!isMatchCategory(providerUrl.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY),
                consumerUrl.getParameter(Constants.CATEGORY_KEY, Constants.DEFAULT_CATEGORY))) {
            return false;
        }
        if (!providerUrl.getParameter(Constants.ENABLED_KEY, true)
                && !Constants.ANY_VALUE.equals(consumerUrl.getParameter(Constants.ENABLED_KEY))) {
            return false;
        }

        String consumerGroup = consumerUrl.getParameter(Constants.GROUP_KEY);
        String consumerVersion = consumerUrl.getParameter(Constants.VERSION_KEY);
        String consumerClassifier = consumerUrl.getParameter(Constants.CLASSIFIER_KEY, Constants.ANY_VALUE);

        String providerGroup = providerUrl.getParameter(Constants.GROUP_KEY);
        String providerVersion = providerUrl.getParameter(Constants.VERSION_KEY);
        String providerClassifier = providerUrl.getParameter(Constants.CLASSIFIER_KEY, Constants.ANY_VALUE);
        return (Constants.ANY_VALUE.equals(consumerGroup) ||
                consumerGroup.equals(providerGroup) ||
                isContains(consumerGroup, providerGroup))
                && (Constants.ANY_VALUE.equals(consumerVersion) ||
                consumerVersion.equals(providerVersion))
                && (consumerClassifier == null ||
                Constants.ANY_VALUE.equals(consumerClassifier) ||
                consumerClassifier.equals(providerClassifier));
    }

    public static boolean isServiceKeyMatch(URL pattern, URL value) {
        return pattern.getParameter(Constants.INTERFACE_KEY).equals(
                value.getParameter(Constants.INTERFACE_KEY))
                && isItemMatch(pattern.getParameter(Constants.GROUP_KEY),
                value.getParameter(Constants.GROUP_KEY))
                && isItemMatch(pattern.getParameter(Constants.VERSION_KEY),
                value.getParameter(Constants.VERSION_KEY));
    }

    /**
     * 判断 value 是否匹配 pattern，pattern 支持 * 通配符.
     *
     * @param pattern pattern
     * @param value   value
     * @return true if match otherwise false
     */
    static boolean isItemMatch(String pattern, String value) {
        if (pattern == null) {
            return value == null;
        } else {
            return "*".equals(pattern) || pattern.equals(value);
        }
    }

    public static boolean isContains(String values, String value) {
        if (values == null || values.length() == 0) {
            return false;
        }
        return isContains(Constants.COMMA_SPLIT_PATTERN.split(values), value);
    }

    /**
     * @param values
     * @param value
     * @return contains
     */
    public static boolean isContains(String[] values, String value) {
        if (value != null && value.length() > 0 && values != null
                && values.length > 0) {
            for (String v : values) {
                if (value.equals(v)) {
                    return true;
                }
            }
        }
        return false;
    }

}