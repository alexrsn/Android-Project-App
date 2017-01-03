package com.example.alex.reminderandroidproject;

class DrawerMenuItem {
    private String note;
    private int iconImage;

    DrawerMenuItem(String note, int iconImage) {
        this.note = note;
        this.iconImage = iconImage;
    }

    String getNote() {
        return note;
    }

    int getIconImage() {
        return iconImage;
    }
}
