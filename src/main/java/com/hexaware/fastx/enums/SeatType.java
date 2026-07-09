package com.hexaware.fastx.enums;


public enum SeatType {

    LP("LP"),
    UP("UP"),
    SIDE_LOWER("SL"),
    SIDE_UPPER("SU"),

    WINDOW_SEAT("WS"),
    AISLE_SEAT("AS"),
    MIDDLE_SEAT("MS"),
    SE("SE");

    private final String code;

    SeatType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
