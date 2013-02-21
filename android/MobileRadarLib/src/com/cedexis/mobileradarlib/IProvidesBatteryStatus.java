package com.cedexis.mobileradarlib;

public interface IProvidesBatteryStatus {
    int getBatteryStatus();
    float getBatteryLevel();
    boolean isBatteryCharging();
    float getMinimumBatteryLevel();
    float getMinimumBatteryLevelWhenCharging();
}
