package nl.duflex.proxy;

import org.json.JSONObject;

import java.util.Arrays;

public class DXJsonUtils {
    public static Object GetRecursive(JSONObject jsonObject, final String path) {
        final String[] keys = path.split("\\.");

        for (var i = 0; i < keys.length - 1; ++i) {
            final String key = keys[i];

            if (!jsonObject.has(key)) return null;

            final Object object = jsonObject.get(key);
            if (object instanceof JSONObject newJsonObject) jsonObject = newJsonObject;
            else throw new RuntimeException("Recursion not possible with path " + path);
        }

        if (!jsonObject.has(keys[keys.length - 1])) return null;

        return jsonObject.get(keys[keys.length - 1]);
    }
}
