package com.example.client.module.settings;
public abstract class Setting<T> {
    private final String name, description;
    protected T value;
    public Setting(String name, String description, T def) { this.name=name; this.description=description; this.value=def; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public T getValue() { return value; }
    public void setValue(T v) { this.value = v; }
}
