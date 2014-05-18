package com.example.network;

public enum Command {
	DRAW(0), UNDO(1), REDO(2);
	
	private int val;
	
    private Command(int val) {
        this.val = val;
    }

    public int getVal() {
        return val;
    }

	public static Command getCommand(int comm) {
		switch(comm) {
			case 0:
				return DRAW;
			case 1:
				return UNDO;
			case 2:
				return REDO;
			default:
				return null;
		}
	}
}
