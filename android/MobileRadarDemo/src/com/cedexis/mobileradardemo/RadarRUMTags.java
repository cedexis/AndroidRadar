package com.cedexis.mobileradardemo;

class RadarRUMTags {
    
    private int _value;
    
    protected RadarRUMTags(int value) {
        this._value = value;
    }
    
    public static final RadarRUMTags MainPage = new RadarRUMTags(0x00000001);
    public static final RadarRUMTags ShowLogPage = new RadarRUMTags(0x00000002);
    public static final RadarRUMTags Miscellaneous = new RadarRUMTags(0x00000004);
    
    public int getValue() {
        return this._value;
    }
}
