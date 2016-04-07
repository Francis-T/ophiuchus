package net.oukranos.ophiuchus.types;

import net.oukranos.ophiuchus.utils.Logger;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by francis on 3/24/16.
 */
public class AnswerableControlData {
    private String _name = "";
    private String _label = "";
    private String _controlType = "unknown";
    private String _dataType = "unknown";
    private String _group = "";
    private String _controlRestrictions = "unknown";
    private String _value = "";
    private String _suffix = "";

    public AnswerableControlData(String label, String type) {
        _name = label.toLowerCase().replace(" ", "_");
        _label = label;
        _controlType = type.toLowerCase();
        return;
    }

    public void setName(String name) {
        _name = name.toLowerCase().replace(" ", "_");
    }

    public void setLabel(String label) {
        _label = label;
    }

    public void setControlType(String type) {
        _controlType = type.toLowerCase();
    }

    public void setDataType(String type) {
        _dataType = type.toLowerCase();
    }

    public void setGroup(String group) {
        _group = group;
    }

    public void setRestrictions(String restrictions) {
        _controlRestrictions = restrictions;
    }

    public void setValue(String value) {
        if (getDataType().equals("string")) {
            _value = value;
            return;
        }

        _value = value.replace(" " + getSuffix(), "");
        return;
    }

    public void setSuffix(String suffix) {
        _suffix = suffix;
    }

    public String getName() {
        return _name;
    }

    public String getLabel() { return _label; }

    public String getControlType() {
        return _controlType;
    }

    public String getDataType() {
        return _dataType;
    }

    public String getGroup() {
        return _group;
    }

    public String getRestrictions() {
        return _controlRestrictions;
    }

    public String getValue() {
        return _value;
    }

    public String getSuffix() {
        return _suffix;
    }

    public String getJsonString() {
        JSONObject jsonObject = getJsonObject();
        if (jsonObject == null) {
            return "";
        }
        return jsonObject.toString();
    }

    public JSONObject getJsonObject() {
        JSONObject jsonObject = new JSONObject();

        try {
            jsonObject.put("name", getName());
            jsonObject.put("type", getDataType());
            jsonObject.put("value", getValue());
            jsonObject.put("suffix", getSuffix());
        } catch (JSONException e) {
            Logger.err("JSONException Occurred: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Logger.err("Exception Occurred: " + e.getMessage());
            return null;
        }
        return jsonObject;
    }
}
