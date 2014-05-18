package com.example.brush;

import com.example.whiteboard.R;

public class BrushFactory {

	public final static String[] names = {"Line", "Circle", "Disc", "Square"};
	public final static int[] images = {R.drawable.line, R.drawable.circle, R.drawable.disk, R.drawable.square};
	
	public static Brush getBrush(int idx) {
		switch (idx) {
        	case 0:
        		return new PenBrush();
        	case 1:
        		return new CircleBrush();
        	case 2:
        		return new DiscBrush();
        	case 3:
        		return new SquareBrush();
        	default:
        		/* Return a PenBrush if no other options match. */
        		return new PenBrush();
        }
	}
	
	public static int getBrushIdx(Brush b) {
		if (b instanceof PenBrush)
			return 0;
		else if (b instanceof CircleBrush)
			return 1;
		else if (b instanceof DiscBrush)
			return 2;
		else if (b instanceof SquareBrush)
			return 3;
		else
			return 0;
	}
}
