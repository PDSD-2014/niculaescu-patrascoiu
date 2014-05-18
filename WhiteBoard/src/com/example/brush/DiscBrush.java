package com.example.brush;

import android.graphics.Path;

public class DiscBrush extends Brush {

    @Override
    public void mouseMove(Path path, float x, float y) {
    	path.addCircle(x, y, 8, Path.Direction.CW);
    	path.addCircle(x, y, 6, Path.Direction.CW);
    	path.addCircle(x, y, 4, Path.Direction.CW);
        path.addCircle(x, y, 1, Path.Direction.CW);
    }
}
