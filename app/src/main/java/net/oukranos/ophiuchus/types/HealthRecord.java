package net.oukranos.ophiuchus.types;

import net.oukranos.ophiuchus.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by francis on 3/26/16.
 */
public class HealthRecord {
    private Map<String, String> _healthDataMap = null;

    public HealthRecord(String json) {
        _healthDataMap =  new HashMap<String, String>();
        parseJsonString(json, _healthDataMap);
        return;
    }

    public Map<String, String> getHealthDataMap() {
        return _healthDataMap;
    }

    private void parseJsonString(String data, Map<String, String> dataMap) {
        if (dataMap == null) {
            return;
        }

        /* Clear the container */
        dataMap.clear();

        try {
            JSONObject jsonObject = new JSONObject(data);
            JSONArray detailInfo;
            String key;
            String value;

            key = "id";
            value = jsonObject.getString("id");
            dataMap.put(key, value);

            detailInfo = jsonObject.getJSONArray("general info");
            for (int iIdx = 0; iIdx < detailInfo.length(); iIdx++) {
                JSONObject arrObject = detailInfo.getJSONObject(iIdx);
                key = arrObject.getString("name").replace("_", " ");
                value = arrObject.getString("value");
                if (value.length() > 10) {
                    value = value.substring(0,10);
                }

                dataMap.put(key, value);
            }

            detailInfo = jsonObject.getJSONArray("medical history");
            for (int iIdx = 0; iIdx < detailInfo.length(); iIdx++) {
                JSONObject arrObject = detailInfo.getJSONObject(iIdx);
                key = arrObject.getString("name").replace("_", " ");
                value = arrObject.getString("value");
                if (value.length() > 10) {
                    value = value.substring(0,10);
                }

                dataMap.put(key, value);
            }

            detailInfo = jsonObject.getJSONArray("checkup details");
            for (int iIdx = 0; iIdx < detailInfo.length(); iIdx++) {
                JSONObject arrObject = detailInfo.getJSONObject(iIdx);
                key = arrObject.getString("name").replace("_", " ");
                value = arrObject.getString("value");
                if (value.length() > 10) {
                    value = value.substring(0,10);
                }

                dataMap.put(key, value);
            }
        } catch (JSONException e) {
            Logger.err("JSONException Occurred: " + e.getMessage());
            return;
        } catch (Exception e) {
            Logger.err("Exception Occurred: " + e.getMessage());
            return;
        }

        return;
    }

    @Override
    public String toString() {
        String result = "";

        Set<String> keys = _healthDataMap.keySet();
        boolean isFirst = true;
        for (String key : keys) {
            if (isFirst) {
                isFirst = false;
            } else {
                result += ", ";
            }

            result += key;
            result += ": ";
            result += _healthDataMap.get(key);
        }

        return result;
    }

}
