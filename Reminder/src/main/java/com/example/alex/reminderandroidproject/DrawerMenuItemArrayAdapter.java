package com.example.alex.reminderandroidproject;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

class DrawerMenuItemArrayAdapter extends ArrayAdapter<DrawerMenuItem> {

    private Activity activity;
    private DrawerMenuItem[] drawerMenuItems;

    DrawerMenuItemArrayAdapter(Activity activity, DrawerMenuItem[] drawerMenuItems) {
        super(activity, R.layout.item_menu_drawer, drawerMenuItems);
        this.activity = activity;
        this.drawerMenuItems = drawerMenuItems;
    }

    private static class ViewContainer{
        ImageView imgIcon;
        TextView lblNote;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewContainer viewContainer;
        View rowView = convertView;
        if(rowView == null){
            LayoutInflater inflater = activity.getLayoutInflater();
            rowView = inflater.inflate(R.layout.item_menu_drawer, parent, false);
            viewContainer = new ViewContainer();
            viewContainer.lblNote = (TextView) rowView.findViewById(R.id.lblNote);
            viewContainer.imgIcon = (ImageView)rowView.findViewById(R.id.imgIcon);
            rowView.setTag(viewContainer);
        }else{
            viewContainer = (ViewContainer)rowView.getTag();
        }

        viewContainer.lblNote.setText(drawerMenuItems[position].getNote());
        viewContainer.imgIcon.setImageResource(drawerMenuItems[position].getIconImage());


        return rowView;
    }
}