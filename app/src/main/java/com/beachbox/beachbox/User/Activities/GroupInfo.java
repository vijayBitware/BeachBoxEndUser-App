package com.beachbox.beachbox.User.Activities;

import java.util.ArrayList;

/**
 * Created by bitwarepc on 20-Jul-17.
 */

public class GroupInfo {
    private String name;
    private ArrayList<ChildInfo> list = new ArrayList<ChildInfo>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ArrayList<ChildInfo> getProductList() {
        return list;
    }

    public void setProductList(ArrayList<ChildInfo> productList) {
        this.list = productList;
    }

}
