package com.example.android.captureframes;

import android.content.res.AssetManager;
import android.graphics.Color;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Vector;

/**
 * Created by shafy on 09/07/2018.
 */

public class ClassAttrProvider {
    private final Vector<String> labels = new Vector();
    private final Vector<Integer> colors = new Vector();
    private static ClassAttrProvider instance;

    private ClassAttrProvider(final AssetManager assetManager) {
        init(assetManager);
    }

    public static ClassAttrProvider newInstance(final AssetManager assetManager) {
        if (instance == null) {
            instance = new ClassAttrProvider(assetManager);
        }

        return instance;
    }

    private void init(final AssetManager assetManager) {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(assetManager.open("tiny-yolo-voc-labels.txt")))) {
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
                colors.add(convertClassNameToColor(line));
            }
        } catch (IOException ex) {
            throw new RuntimeException("Problem reading label file!", ex);
        }
    }

    private int convertClassNameToColor(String className) {
        byte[] rgb = new byte[3];
        byte[] name = className.getBytes();

        for (int i=0; i<name.length; i++) {
            rgb[i%3] += name[i];
        }

        // Hue saturation
        for (int i=0; i<rgb.length; i++) {
            if (rgb[i] < 120) {
                rgb[i] += 120;
            }
        }

        return Color.rgb(rgb[0], rgb[1], rgb[2]);
    }

    public Vector<String> getLabels() {
        return labels;
    }

    public Vector<Integer> getColors() {
        return colors;
    }
}