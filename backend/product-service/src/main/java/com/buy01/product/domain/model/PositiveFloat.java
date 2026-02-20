package com.buy01.product.domain.model;

public class PositiveFloat {
    private final float value;

    public PositiveFloat(float value) {
        if (value <= 0.0f) {
            throw new IllegalArgumentException("Value must be positive.");
        }
        this.value = value;
    }

    public float getValue() {
        return value;
    }
}
