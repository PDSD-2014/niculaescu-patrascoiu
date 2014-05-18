package com.example.brush;

import android.graphics.Path;

public class DiscBrush extends Brush {

    @Override
    public void mouseMove(Path path, float x, float y) {
        path.addCircle(x, y, 2, Path.Direction.CW);
    }
}
