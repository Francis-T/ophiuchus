package net.oukranos.ophiuchus.types;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import net.oukranos.ophiuchus.R;
import net.oukranos.ophiuchus.interfaces.DataChangeListener;
import net.oukranos.ophiuchus.utils.Logger;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by francis on 3/24/16.
 */
public class AnswerableControlAdapter extends BaseExpandableListAdapter implements DataChangeListener {
    private Context _context = null;
    private List<GroupedAnswerableControlData> _groupList;
    private boolean _bDataChanged = false;
    private String _refId;

    public AnswerableControlAdapter(Context context,
                                    String refId,
                                    List<AnswerableControlData> items,
                                    List<String> groups) {
        _context = context;
        _groupList = new ArrayList<GroupedAnswerableControlData>();
        _refId = refId;

        int iGrouped = 0;
        for (String groupName : groups) {
            GroupedAnswerableControlData dataGroup =
                    new GroupedAnswerableControlData(groupName);

            for (AnswerableControlData dataItem : items) {
                /* If the item's group name matches, add it to the group */
                if (dataItem.getGroup().equals(groupName)) {
                    dataGroup.addData(dataItem);
                    Logger.dbg("Adding " + dataItem.getName() + " to " + groupName);
                    iGrouped++;
                }
            }

            /* Finally, add the group to our list */
            _groupList.add(dataGroup);
        }

        if (iGrouped != items.size()) {
            Logger.warn("Some items may not have been added: Expected " +
                    items.size() + " items, Added " + iGrouped + " items");
        }

        return;
    }

    @Override
    public int getGroupCount() {
        if (_groupList == null) {
            return 0;
        }
        return _groupList.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (!isValidGroupId(groupPosition)) {
            return 0;
        }
        return _groupList.get(groupPosition).getCount();
    }

    @Override
    public GroupedAnswerableControlData getGroup(int groupPosition) {
        if (!isValidGroupId(groupPosition)) {
            return null;
        }

        return _groupList.get(groupPosition);
    }

    @Override
    public AnswerableControlData getChild(int groupPosition, int childPosition) {
        if (!isValidChildId(groupPosition, childPosition)) {
            return null;
        }

        return _groupList.get(groupPosition).getChild(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        if (!isValidGroupId(groupPosition)) {
            return -1;
        }
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        if (!isValidChildId(groupPosition, childPosition)) {
            return -1;
        }
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView,
                             ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater =
                    (LayoutInflater) _context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_control_group, parent, false);
        }

        GroupedAnswerableControlData groupData = getGroup(groupPosition);
        if (groupData == null) {
            return convertView;
        }

        /* Since we know basically inflated a TextView into convertView,
         *  we can safely cast it as such */
        TextView txvGroupName = (TextView) convertView.findViewById(R.id.txv_ctrl_group_name);
        txvGroupName.setText(groupData.getName());

        return convertView;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild,
                             View convertView, ViewGroup parent) {
        AnswerableControlData controlData = getChild(groupPosition, childPosition);
        if (controlData == null) {
            return convertView;
        }

        return AnswerableControlViewCreator.
                createView(_context, controlData, convertView, parent, this);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    public boolean isDataChanged() {
        return _bDataChanged;
    }

    public void onDataChanged(String source) {
        Logger.info("Data changed: " + source);
        _bDataChanged  = true;
        return;
    }

    /* *************** */
    /* PRIVATE METHODS */
    /* *************** */
    private boolean isValidGroupId(int id) {
        if (_groupList == null) {
            Logger.err("Group list unavailable");
            return false;
        }

        if ((id < 0) || (id > _groupList.size())) {
            Logger.err("Invalid group id: " + id);
            return false;
        }

        return true;
    }

    private boolean isValidChildId(int groupId, int childId) {
        if (!isValidGroupId(groupId)) {
            return false;
        }

        return !((childId < 0) || (childId > _groupList.get(groupId).getCount()));

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
            if (_refId == null) {
                _refId = "999999";
            }

            jsonObject.put("id", _refId);

            for (GroupedAnswerableControlData group : _groupList) {
                jsonObject.put(group.getName().toLowerCase(), group.getJsonArray());
            }
        } catch (JSONException e) {
            Logger.err("JSONException Occurred: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Logger.err("Exception Occurred: " + e.getMessage());
            return null;
        }
        return jsonObject;

    }
    /* ********************* */
    /* PRIVATE INNER CLASSES */
    /* ********************* */
    private class GroupedAnswerableControlData  {
        private String _name = "";
        private List<AnswerableControlData> _children = null;

        public GroupedAnswerableControlData(String name) {
            _name = name;
            _children = new ArrayList<AnswerableControlData>();
            return;
        }

        public void addData(AnswerableControlData data) {
            if (data == null) {
                Logger.err("Cannot add NULL control data to list");
                return;
            }
            _children.add(data);
            return;
        }

        public int getCount() {
            return _children.size();
        }

        public AnswerableControlData getChild(int id) {
            if ((id < 0) || (id > getCount())) {
                return null;
            }

            return _children.get(id);
        }

        public String getName() {
            return _name;
        }

        public String getJsonString() {
            JSONArray jsonArray = getJsonArray();
            if (jsonArray == null) {
                return "";
            }

            return jsonArray.toString();
        }

        public JSONArray getJsonArray() {
            JSONArray jsonArray = new JSONArray();

            for (AnswerableControlData child : _children) {
                jsonArray.put(child.getJsonObject());
            }

            return jsonArray;

        }
    }
}
