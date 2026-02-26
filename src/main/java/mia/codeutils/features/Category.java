package mia.codeutils.features;

import java.util.ArrayList;

public class Category {
    private final String name;
    private final ArrayList<Feature> features;

    public Category(String name) {
        this.name = name;
        this.features = new ArrayList<>();
    }

    public String getName() { return this.name; }

    public void addFeature(Feature feature) { features.add(feature); feature.setCategory(this); }
    public ArrayList<Feature> getFeatures() { return this.features; }
}
