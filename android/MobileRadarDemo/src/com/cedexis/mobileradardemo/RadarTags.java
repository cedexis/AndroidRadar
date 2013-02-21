package com.cedexis.mobileradardemo;

class RadarTags {
    
    private int _value;
    
    protected RadarTags(int value) {
        this._value = value;
    }
    
    public static final RadarTags MainPage = new RadarTags(0x00000001);
    public static final RadarTags ShowLogPage = new RadarTags(0x00000002);
    public static final RadarTags Miscellaneous = new RadarTags(0x00000004);
    
    public int getValue() {
        return this._value;
    }
}
