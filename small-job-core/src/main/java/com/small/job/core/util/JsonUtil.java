package com.small.job.core.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;

/**
 * @author null
 * @version 1.0
 * @title
 * @description
 * @createDate 12/30/19 10:10 AM
 */
public class JsonUtil {
    private static final JsonUtil.JsonReader jsonReader = new JsonUtil.JsonReader();
    private static final JsonUtil.Jsonwriter jsonwriter = new JsonUtil.Jsonwriter();

    public JsonUtil() {
    }

    public static String toJson(Object object) {
        return jsonwriter.toJson(object);
    }

    public static Map<String, Object> parseMap(String json) {
        return jsonReader.parseMap(json);
    }

    public static List<Object> parseList(String json) {
        return jsonReader.parseList(json);
    }

    private static class Jsonwriter {
        private static Logger logger = LoggerFactory.getLogger(JsonUtil.Jsonwriter.class);
        private static final String STR_SLASH = "\"";
        private static final String STR_SLASH_STR = "\":";
        private static final String STR_COMMA = ",";
        private static final String STR_OBJECT_LEFT = "{";
        private static final String STR_OBJECT_RIGHT = "}";
        private static final String STR_ARRAY_LEFT = "[";
        private static final String STR_ARRAY_RIGHT = "]";
        private static final Map<String, Field[]> cacheFields = new HashMap();

        private Jsonwriter() {
        }

        public String toJson(Object object) {
            StringBuilder json = new StringBuilder();

            try {
                this.writeObjItem((String) null, object, json);
            } catch (Exception var4) {
                logger.error(var4.getMessage(), var4);
            }

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

        private void writeObjItem(String key, Object value, StringBuilder json) {
            if (key != null) {
                json.append("\"").append(key).append("\":");
            }

            if (value == null) {
                json.append("null");
            } else if (!(value instanceof String) && !(value instanceof Byte) && !(value instanceof CharSequence)) {
                if (!(value instanceof Boolean) && !(value instanceof Short) && !(value instanceof Integer) && !(value instanceof Long) && !(value instanceof Float) && !(value instanceof Double)) {
                    if (!(value instanceof Object[]) && !(value instanceof Collection)) {
                        if (value instanceof Map) {
                            Map<?, ?> valueMap = (Map) value;
                            json.append("{");
                            if (!valueMap.isEmpty()) {
                                Set<?> keys = valueMap.keySet();
                                Iterator var15 = keys.iterator();

                                while (var15.hasNext()) {
                                    Object valueMapItemKey = var15.next();
                                    this.writeObjItem(valueMapItemKey.toString(), valueMap.get(valueMapItemKey), json);
                                    json.append(",");
                                }

                                json.delete(json.length() - 1, json.length());
                            }

                            json.append("}");
                        } else {
                            json.append("{");
                            Field[] fields = this.getDeclaredFields(value.getClass());
                            if (fields.length > 0) {
                                Field[] var14 = fields;
                                int var16 = fields.length;

                                for (int var17 = 0; var17 < var16; ++var17) {
                                    Field field = var14[var17];
                                    Object fieldObj = this.getFieldObject(field, value);
                                    this.writeObjItem(field.getName(), fieldObj, json);
                                    json.append(",");
                                }

                                json.delete(json.length() - 1, json.length());
                            }

                            json.append("}");
                        }
                    } else {
                        Collection valueColl = null;
                        if (value instanceof Object[]) {
                            Object[] valueArr = (Object[]) value;
                            valueColl = Arrays.asList(valueArr);
                        } else if (value instanceof Collection) {
                            valueColl = (Collection) value;
                        }

                        json.append("[");
                        if (((Collection) valueColl).size() > 0) {
                            Iterator var11 = ((Collection) valueColl).iterator();

                            while (var11.hasNext()) {
                                Object obj = var11.next();
                                this.writeObjItem((String) null, obj, json);
                                json.append(",");
                            }

                            json.delete(json.length() - 1, json.length());
                        }

                        json.append("]");
                    }
                } else {
                    json.append(value);
                }
            } else {
                json.append("\"").append(value.toString()).append("\"");
            }

        }

        public synchronized Field[] getDeclaredFields(Class<?> clazz) {
            String cacheKey = clazz.getName();
            if (cacheFields.containsKey(cacheKey)) {
                return (Field[]) cacheFields.get(cacheKey);
            } else {
                Field[] fields = this.getAllDeclaredFields(clazz);
                cacheFields.put(cacheKey, fields);
                return fields;
            }
        }

        private Field[] getAllDeclaredFields(Class<?> clazz) {
            List<Field> list = new ArrayList();

            for (Class current = clazz; current != null && current != Object.class; current = current.getSuperclass()) {
                Field[] fields = current.getDeclaredFields();
                Field[] var5 = fields;
                int var6 = fields.length;

                for (int var7 = 0; var7 < var6; ++var7) {
                    Field field = var5[var7];
                    if (!Modifier.isStatic(field.getModifiers())) {
                        list.add(field);
                    }
                }
            }

            return (Field[]) list.toArray(new Field[list.size()]);
        }

        private synchronized Object getFieldObject(Field field, Object obj) {
            Object var4;
            try {
                field.setAccessible(true);
                Object var3 = field.get(obj);
                return var3;
            } catch (IllegalAccessException | IllegalArgumentException var8) {
                logger.error(var8.getMessage(), var8);
                var4 = null;
            } finally {
                field.setAccessible(false);
            }

            return var4;
        }
    }

    private static class JsonReader {
        private JsonReader() {
        }

        public Map<String, Object> parseMap(String json) {
            if (json != null) {
                json = json.trim();
                if (json.startsWith("{")) {
                    return this.parseMapInternal(json);
                }
            }

            throw new IllegalArgumentException("Cannot parse JSON");
        }

        public List<Object> parseList(String json) {
            if (json != null) {
                json = json.trim();
                if (json.startsWith("[")) {
                    return this.parseListInternal(json);
                }
            }

            throw new IllegalArgumentException("Cannot parse JSON");
        }

        private List<Object> parseListInternal(String json) {
            List<Object> list = new ArrayList();
            json = trimLeadingCharacter(trimTrailingCharacter(json, ']'), '[');
            Iterator var3 = this.tokenize(json).iterator();

            while (var3.hasNext()) {
                String value = (String) var3.next();
                list.add(this.parseInternal(value));
            }

            return list;
        }

        private Object parseInternal(String json) {
            if (json.equals("null")) {
                return null;
            } else if (json.startsWith("[")) {
                return this.parseListInternal(json);
            } else if (json.startsWith("{")) {
                return this.parseMapInternal(json);
            } else if (json.startsWith("\"")) {
                return trimTrailingCharacter(trimLeadingCharacter(json, '"'), '"');
            } else {
                try {
                    return Long.valueOf(json);
                } catch (NumberFormatException var4) {
                    try {
                        return Double.valueOf(json);
                    } catch (NumberFormatException var3) {
                        return json;
                    }
                }
            }
        }

        private Map<String, Object> parseMapInternal(String json) {
            Map<String, Object> map = new LinkedHashMap();
            json = trimLeadingCharacter(trimTrailingCharacter(json, '}'), '{');
            Iterator var3 = this.tokenize(json).iterator();

            while (var3.hasNext()) {
                String pair = (String) var3.next();
                String[] values = trimArrayElements(split(pair, ":"));
                String key = trimLeadingCharacter(trimTrailingCharacter(values[0], '"'), '"');
                Object value = this.parseInternal(values[1]);
                map.put(key, value);
            }

            return map;
        }

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
            if (array != null && array.length != 0) {
                String[] result = new String[array.length];

                for (int i = 0; i < array.length; ++i) {
                    String element = array[i];
                    result[i] = element != null ? element.trim() : null;
                }

                return result;
            } else {
                return new String[0];
            }
        }

        private List<String> tokenize(String json) {
            List<String> list = new ArrayList();
            int index = 0;
            int inObject = 0;
            int inList = 0;
            boolean inValue = false;
            boolean inEscape = false;
            StringBuilder build = new StringBuilder();

            while (true) {
                while (index < json.length()) {
                    char current = json.charAt(index);
                    if (inEscape) {
                        build.append(current);
                        ++index;
                        inEscape = false;
                    } else {
                        if (current == '{') {
                            ++inObject;
                        }

                        if (current == '}') {
                            --inObject;
                        }

                        if (current == '[') {
                            ++inList;
                        }

                        if (current == ']') {
                            --inList;
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

                        ++index;
                    }
                }

                if (build.length() > 0) {
                    list.add(build.toString());
                }

                return list;
            }
        }

        private static String trimTrailingCharacter(String string, char c) {
            return string.length() > 0 && string.charAt(string.length() - 1) == c ? string.substring(0, string.length() - 1) : string;
        }

        private static String trimLeadingCharacter(String string, char c) {
            return string.length() > 0 && string.charAt(0) == c ? string.substring(1) : string;
        }
    }

}
