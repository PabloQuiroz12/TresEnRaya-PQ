package edu.upb.tresenraya.utils;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

public class ColorUtils {

    private static final Map<String, Color> colorMap = new HashMap<>();

    static {
        colorMap.put("BLACK", Color.BLACK);
        colorMap.put("WHITE", Color.WHITE);
        colorMap.put("RED", Color.RED);
        colorMap.put("GREEN", Color.GREEN);
        colorMap.put("BLUE", Color.BLUE);
        colorMap.put("YELLOW", Color.YELLOW);
        colorMap.put("CYAN", Color.CYAN);
        colorMap.put("MAGENTA", Color.MAGENTA);
        colorMap.put("ORANGE", Color.ORANGE);
        colorMap.put("GRAY", Color.GRAY);
        colorMap.put("LIGHT_GRAY", Color.LIGHT_GRAY);
        colorMap.put("DARK_GRAY", Color.DARK_GRAY);
    }

    public static Color getColorFromString(String colorName) {
        return colorMap.getOrDefault(colorName.toUpperCase(), Color.WHITE);
    }
}
