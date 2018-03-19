package com.beachbox.beachbox.expandable;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CheckBox;
import android.widget.CheckedTextView;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.beachbox.beachbox.R;
import com.beachbox.beachbox.User.Activities.UDashboardActivityNew;
import com.beachbox.beachbox.User.Fragments.FragmentFilter;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Created by Gourav on 08-03-2016.
 */
public class CustomAdapter1 extends BaseExpandableListAdapter {

    private Context context;
    private ArrayList<GroupInfo1> deptList;
    private HashMap<Integer,boolean[]> hashMap = new HashMap<>();
    MyInterface mInterface;

    public CustomAdapter1(Context context, ArrayList<GroupInfo1> deptList, FragmentFilter fragmentFilter) {
        this.context = context;
        this.deptList = deptList;
        this.mInterface = fragmentFilter;
        prepareCheckState();
    }

    static class ViewHolder {
        protected TextView text, ChildText;
        CheckBox childItem;
    }

    private void prepareCheckState(){
        for (int i = 0; i < deptList.size(); i++) {
            boolean[] listBoolean = new boolean[deptList.get(i).getProductList().size()];
            hashMap.put(i,listBoolean);
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        List<Menu> productList = deptList.get(groupPosition).getProductList();
        return productList.get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild,
                             View view, ViewGroup parent) {

        ViewHolder viewHolder = null;
        final Menu detailInfo = (Menu) getChild(groupPosition, childPosition);
        if (view == null) {
            LayoutInflater infalInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = infalInflater.inflate(R.layout.filter_child_row, null);

            viewHolder = new ViewHolder();
            viewHolder.childItem = (CheckBox) view.findViewById(R.id.childItem);
            viewHolder.ChildText = (TextView) view.findViewById(R.id.childText);
            viewHolder.ChildText.setText(detailInfo.getName());
            view.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder)view.getTag();
            boolean[] booleen = hashMap.get(groupPosition);
            viewHolder.childItem.setChecked(booleen[childPosition]);
            viewHolder.ChildText.setText(detailInfo.getName());
        }
        viewHolder.childItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                detailInfo.setGroupPosition(groupPosition);
                boolean[] listBoolean = hashMap.get(groupPosition);
                if(((CheckBox)v).isChecked()) {
                    listBoolean[childPosition] = true;
                    hashMap.put(groupPosition,listBoolean);
                    mInterface.OnCheck(groupPosition,detailInfo, "add");
                   // Toast.makeText(context, "Checked", Toast.LENGTH_SHORT).show();
                } else {
                    listBoolean[childPosition] = false;
                    hashMap.put(groupPosition,listBoolean);
                    mInterface.OnCheck(groupPosition,detailInfo, "remove");
                }
            }
        });
        return view;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        List<Menu> productList = deptList.get(groupPosition).getProductList();
        return productList.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return deptList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return deptList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isLastChild, View view,
                             ViewGroup parent) {

        ViewHolder viewHolder = null;
        GroupInfo1 headerInfo = (GroupInfo1) getGroup(groupPosition);
        if (view == null) {
            LayoutInflater inf = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            view = inf.inflate(R.layout.group_items, null);
        }
        viewHolder = new ViewHolder();
        viewHolder.text = (TextView) view.findViewById(R.id.heading);
        viewHolder.text.setText(headerInfo.getName().trim());


        view.setTag(viewHolder);
        return view;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    public interface MyInterface {

        public void OnCheck(int groupPosition, Menu menu, String action);

    }

}
