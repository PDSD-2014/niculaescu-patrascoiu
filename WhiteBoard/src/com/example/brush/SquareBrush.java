package com.example.brush;

import android.graphics.Path;

public class SquareBrush extends Brush {

    @Override
    public void mouseMove(Path path, float x, float y) {
        path.addRect(x - 5, y - 5, x + 5, y + 5,Path.Direction.CW);
    }
}
