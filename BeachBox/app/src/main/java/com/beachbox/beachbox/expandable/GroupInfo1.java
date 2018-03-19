package com.beachbox.beachbox.expandable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bitwarepc on 20-Jul-17.
 */

public class GroupInfo1 {
    private String name;
    private List<Menu> list = new ArrayList<Menu>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Menu> getProductList() {
        return list;
    }

    public void setProductList(List<Menu> productList) {
        this.list = productList;
    }

}