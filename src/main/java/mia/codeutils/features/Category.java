package mia.codeutils.features;

import java.util.ArrayList;

public class Category {
    private final String name;
    private final String description;
    private final ArrayList<Feature> features;


    public Category(String name, String description) {
        this.name = name;
        this.description = description;
        this.features = new ArrayList<>();
    }

    public String getName() { return this.name; }
    public String getDescription() { return this.description; }

    public void addFeature(Feature feature) { features.add(feature); feature.setCategory(this); }
    public ArrayList<Feature> getFeatures() { return this.features; }
}
