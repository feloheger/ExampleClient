package com.example.client.module.settings;
public class SliderSetting extends Setting<Double> {
    private final double min, max;
    public SliderSetting(String name, String desc, double def, double min, double max) {
        super(name, desc, def); this.min=min; this.max=max;
    }
    public double getMin() { return min; }
    public double getMax() { return max; }
    @Override public void setValue(Double v) { this.value = Math.max(min, Math.min(max, v)); }
}
