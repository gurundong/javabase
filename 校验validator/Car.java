package com.grd.example.validate;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class Car {
    // 制造商
    @NotNull
    private String manufacturer;

    // 拍照
    @NotNull
    @CheckCase(CaseMode.UPPER)
    private String licensePlate;

    // 座位数
    @Min(2)
    private int seatCount;

    public Car(String manufacturer, String licensePlate,int seatCount) {
        this.manufacturer = manufacturer;
        this.licensePlate = licensePlate;
        this.seatCount = seatCount;
    }
}
