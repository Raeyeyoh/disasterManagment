package com.dms.disastermanagmentapi.Services;

import com.dms.disastermanagmentapi.enums.Unit;

public class UnitConverter {

    public static int toBase(Unit unit, int quantity) {
        return switch (unit) {
            case KG, LITER, PIECE -> quantity;
            case BAG -> quantity * 25;   // 1 bag = 25kg
            case BOX -> quantity * 10;   // 1 box = 10 pieces
        };
    }
}
