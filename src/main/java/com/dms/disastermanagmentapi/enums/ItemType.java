package com.dms.disastermanagmentapi.enums;

public enum ItemType {

    MEDICAL(3),
    FOOD(2),
    EQUIPMENT(1),
    OTHER(1);

    private final int weight;

    ItemType(int weight) {
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}

