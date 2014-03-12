package com.cedexis.mobileradardemo;

class RadarRUMTags {
    
    private long _value;
    
    protected RadarRUMTags(long value) {
        this._value = value;
    }
    
    // In Java we can only use up to 
    public static final RadarRUMTags MainPage = new RadarRUMTags(0x1);
    public static final RadarRUMTags Miscellaneous = new RadarRUMTags(0x2);
    // In Java we can only use up to 63 bits
    public static final RadarRUMTags MaxTag = new RadarRUMTags(0x4000000000000000L);
    
    public long getValue() {
        return this._value;
    }
}
