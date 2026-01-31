package com.dms.disastermanagmentapi.enums;

import java.util.List;

public enum ItemType {
    EQUIPMENT(1, List.of(Unit.PIECE, Unit.BOX)),
    FOOD(2, List.of(Unit.KG, Unit.LITER, Unit.BAG)),
    MEDICAL(3, List.of(Unit.PIECE, Unit.BOX, Unit.LITER)),
    OTHER(1, List.of(Unit.PIECE, Unit.BAG, Unit.BOX));
    private ItemType(int weight, List<Unit> allowedUnits) {
        this.weight = weight;
        this.allowedUnits = allowedUnits;
    }
private final int weight;

    public int getWeight() {
        return weight;
    }
    private final List<Unit> allowedUnits;

   
    public List<Unit> getAllowedUnits() {
        return allowedUnits;
    }

}


