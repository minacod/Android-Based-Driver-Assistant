package com.example.android.captureframes;

/**
 * Created by shafy on 09/07/2018.
 */

public class ClassifiedObject {
    private final Integer id;
    private final String title;
    private final Float confidence;
    private BoxPosition location;

    public ClassifiedObject(final Integer id, final String title,
                       final Float confidence, final BoxPosition location) {
        this.id = id;
        this.title = title;
        this.confidence = confidence;
        this.location = location;
    }

    public Integer getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public Float getConfidence() {
        return confidence;
    }

    public BoxPosition getLocation() {
        return new BoxPosition(location);
    }

    public void setLocation(BoxPosition location) {
        this.location = location;
    }
}
