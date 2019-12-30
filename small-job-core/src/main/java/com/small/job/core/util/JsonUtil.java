package com.small.job.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @ClassName JsonUtil
 * @Description TODO
 * @Author xiangke
 * @Date 2019/12/29 20:52
 * @Version 1.0
 **/
public class JsonUtil {

    private static final JsonReader jsonReader = new JsonReader();
    private static final Jsonwriter jsonwriter = new Jsonwriter();


    /**
     * object to json
     *
     * @param object
     * @return
     */
    public static String toJson(Object object) {
        return jsonwriter.toJson(object);
    }

    /**
     * parse json to map
     *
     * @param json
     * @return only for filed type "null、ArrayList、LinkedHashMap、String、Long、Double、..."
     */
    public static Map<String, Object> parseMap(String json) {
        return jsonReader.parseMap(json);
    }

    /**
     * json to List
     *
     * @param json
     * @return
     */
    public static List<Object> parseList(String json) {
        return jsonReader.parseList(json);
    }

    private static class JsonReader {
        public Map<String, Object> parseMap(String json) {
            if (json != null) {
                json = json.trim();
                if (json.startsWith("{")) {
                    return parseMapInternal(json);
                }
            }
            throw new IllegalArgumentException("Cannot parse JSON");
        }

        public List<Object> parseList(String json) {
            if (json != null) {
                json = json.trim();
                if (json.startsWith("[")) {
                    return parseListInternal(json);
                }
            }
            throw new IllegalArgumentException("Cannot parse JSON");
        }


        private List<Object> parseListInternal(String json) {
            List<Object> list = new ArrayList<>();
            json = trimLeadingCharacter(trimTrailingCharacter(json, ']'), '[');
            for (String value : tokenize(json)) {
                list.add(parseInternal(value));
            }
            return list;
        }

        private Object parseInternal(String json) {
            if (json.equals("null")) {
                return null;
            }
            if (json.startsWith("[")) {
                return parseListInternal(json);
            }
            if (json.startsWith("{")) {
                return parseMapInternal(json);
            }
            if (json.startsWith("\"")) {
                return trimTrailingCharacter(trimLeadingCharacter(json, '"'), '"');
            }
            try {
                return Long.valueOf(json);
            } catch (NumberFormatException ex) {
                // ignore
            }
            try {
                return Double.valueOf(json);
            } catch (NumberFormatException ex) {
                // ignore
            }
            return json;
        }

        private Map<String, Object> parseMapInternal(String json) {
            Map<String, Object> map = new LinkedHashMap<>();
            json = trimLeadingCharacter(trimTrailingCharacter(json, '}'), '{');
            for (String pair : tokenize(json)) {
                String[] values = trimArrayElements(split(pair, ":"));
                String key = trimLeadingCharacter(trimTrailingCharacter(values[0], '"'), '"');
                Object value = parseInternal(values[1]);
                map.put(key, value);
            }
            return map;
        }

        // append start
        private static String[] split(String toSplit, String delimiter) {
            if (toSplit != null && !toSplit.isEmpty() && delimiter != null && !delimiter.isEmpty()) {
                int offset = toSplit.indexOf(delimiter);
                if (offset < 0) {
                    return null;
                } else {
                    String beforeDelimiter = toSplit.substring(0, offset);
                    String afterDelimiter = toSplit.substring(offset + delimiter.length());
                    return new String[]{beforeDelimiter, afterDelimiter};
                }
            } else {
                return null;
            }
        }

        private static String[] trimArrayElements(String[] array) {
            if (array == null || array.length == 0) {
                return new String[0];
            } else {
                String[] result = new String[array.length];

                for (int i = 0; i < array.length; ++i) {
                    String element = array[i];
                    result[i] = element != null ? element.trim() : null;
                }

                return result;
            }
        }
        // append end


        private List<String> tokenize(String json) {
            List<String> list = new ArrayList<String>();
            int index = 0;
            int inObject = 0;
            int inList = 0;
            boolean inValue = false;
            boolean inEscape = false;
            StringBuilder build = new StringBuilder();
            while (index < json.length()) {
                char current = json.charAt(index);
                if (inEscape) {
                    build.append(current);
                    index++;
                    inEscape = false;
                    continue;
                }
                if (current == '{') {
                    inObject++;
                }
                if (current == '}') {
                    inObject--;
                }
                if (current == '[') {
                    inList++;
                }
                if (current == ']') {
                    inList--;
                }
                if (current == '"') {
                    inValue = !inValue;
                }
                if (current == ',' && inObject == 0 && inList == 0 && !inValue) {
                    list.add(build.toString());
                    build.setLength(0);
                } else if (current == '\\') {
                    inEscape = true;
                } else {
                    build.append(current);
                }
                index++;
            }
            if (build.length() > 0) {
                list.add(build.toString());
            }
            return list;
        }

        // plugin util
        private static String trimTrailingCharacter(String string, char c) {
            if (string.length() > 0 && string.charAt(string.length() - 1) == c) {
                return string.substring(0, string.length() - 1);
            }
            return string;
        }

        private static String trimLeadingCharacter(String string, char c) {
            if (string.length() > 0 && string.charAt(0) == c) {
                return string.substring(1);
            }
            return string;
        }
    }

    private static class Jsonwriter {
        private static Logger logger = LoggerFactory.getLogger(Jsonwriter.class);


        private static final String STR_SLASH = "\"";
        private static final String STR_SLASH_STR = "\":";
        private static final String STR_COMMA = ",";
        private static final String STR_OBJECT_LEFT = "{";
        private static final String STR_OBJECT_RIGHT = "}";
        private static final String STR_ARRAY_LEFT = "[";
        private static final String STR_ARRAY_RIGHT = "]";

        private static final Map<String, Field[]> cacheFields = new HashMap<>();

        /**
         * write object to json
         *
         * @param object
         * @return
         */
        public String toJson(Object object) {
            StringBuilder json = new StringBuilder();
            try {
                writeObjItem(null, object, json);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            // replace
            String str = json.toString();
            if (str.contains("\n")) {
                str = str.replaceAll("\\n", "\\\\n");
            }
            if (str.contains("\t")) {
                str = str.replaceAll("\\t", "\\\\t");
            }
            if (str.contains("\r")) {
                str = str.replaceAll("\\r", "\\\\r");
            }
            return str;
        }

        /**
         * append Obj
         *
         * @param key
         * @param value
         * @param json  "key":value or value
         */
        private void writeObjItem(String key, Object value, StringBuilder json) {

            // "key:"
            if (key != null) {
                json.append(STR_SLASH).append(key).append(STR_SLASH_STR);
            }

            // val
            if (value == null) {
                json.append("null");
            } else if (value instanceof String
                    || value instanceof Byte
                    || value instanceof CharSequence) {
                // string

                json.append(STR_SLASH).append(value.toString()).append(STR_SLASH);
            } else if (value instanceof Boolean
                    || value instanceof Short
                    || value instanceof Integer
                    || value instanceof Long
                    || value instanceof Float
                    || value instanceof Double
                    ) {
                // number

                json.append(value);
            } else if (value instanceof Object[] || value instanceof Collection) {
                // collection | array     //  Array.getLength(array);   // Array.get(array, i);

                Collection valueColl = null;
                if (value instanceof Object[]) {
                    Object[] valueArr = (Object[]) value;
                    valueColl = Arrays.asList(valueArr);
                } else if (value instanceof Collection) {
                    valueColl = (Collection) value;
                }

                json.append(STR_ARRAY_LEFT);
                if (valueColl.size() > 0) {
                    for (Object obj : valueColl) {
                        writeObjItem(null, obj, json);
                        json.append(STR_COMMA);
                    }
                    json.delete(json.length() - 1, json.length());
                }
                json.append(STR_ARRAY_RIGHT);

            } else if (value instanceof Map) {
                // map

                Map<?, ?> valueMap = (Map<?, ?>) value;

                json.append(STR_OBJECT_LEFT);
                if (!valueMap.isEmpty()) {
                    Set<?> keys = valueMap.keySet();
                    for (Object valueMapItemKey : keys) {
                        writeObjItem(valueMapItemKey.toString(), valueMap.get(valueMapItemKey), json);
                        json.append(STR_COMMA);
                    }
                    json.delete(json.length() - 1, json.length());

                }
                json.append(STR_OBJECT_RIGHT);

            } else {
                // bean

                json.append(STR_OBJECT_LEFT);
                Field[] fields = getDeclaredFields(value.getClass());
                if (fields.length > 0) {
                    for (Field field : fields) {
                        Object fieldObj = getFieldObject(field, value);
                        writeObjItem(field.getName(), fieldObj, json);
                        json.append(STR_COMMA);
                    }
                    json.delete(json.length() - 1, json.length());
                }

                json.append(STR_OBJECT_RIGHT);
            }
        }

        public synchronized Field[] getDeclaredFields(Class<?> clazz) {
            String cacheKey = clazz.getName();
            if (cacheFields.containsKey(cacheKey)) {
                return cacheFields.get(cacheKey);
            }
            Field[] fields = getAllDeclaredFields(clazz);    //clazz.getDeclaredFields();
            cacheFields.put(cacheKey, fields);
            return fields;
        }

        private Field[] getAllDeclaredFields(Class<?> clazz) {
            List<Field> list = new ArrayList<Field>();
            Class<?> current = clazz;

            while (current != null && current != Object.class) {
                Field[] fields = current.getDeclaredFields();

                for (Field field : fields) {
                    if (Modifier.isStatic(field.getModifiers())) {
                        continue;
                    }
                    list.add(field);
                }

                current = current.getSuperclass();
            }

            return list.toArray(new Field[list.size()]);
        }

        private synchronized Object getFieldObject(Field field, Object obj) {
            try {
                field.setAccessible(true);
                return field.get(obj);
            } catch (IllegalArgumentException | IllegalAccessException e) {
                logger.error(e.getMessage(), e);
                return null;
            } finally {
                field.setAccessible(false);
            }
        }
    }

}
