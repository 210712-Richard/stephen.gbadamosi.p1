package com.revature.model;

public enum Priority {
	LOW(60_000_000), MEDIUM(30_000_000), HIGH(12_000_000), URGENT(6_000_000);
	
	private int timer;
	
	Priority(int time) {
		this.timer = time;		
	}
	
	public String toString(Priority sla) {
		switch(sla) {
			case LOW:
				return "Low (Approximately 10 minutes for auto-approval";
			case MEDIUM:
				return "Medium (Approximately 5 minutes for auto-approval";
			case HIGH:
				return "High (Approximately 2 minutes for auto-approval";
			case URGENT:
				return "Urgent (Approximately 1 minutes for auto-approval";
			default:
				System.out.println("Invalid SLA");
				return null;
		}
	}
	
	public static Priority getPriority(String sla) {
		if(sla.substring(0, 1).equalsIgnoreCase("l")) {
			return LOW;
		}
		
		if(sla.substring(0, 1).equalsIgnoreCase("m")) {
			return MEDIUM;
		}
		
		if(sla.substring(0, 1).equalsIgnoreCase("h")) {
			return HIGH;
		}
		
		if(sla.substring(0, 1).equalsIgnoreCase("u")) {
			return URGENT;
		}
		
		else {
			System.out.println("Invalid SLA input");
			return null;
		}
	}

	public int getTimer() {
		return timer;
	}

	public void setTimer(int timer) {
		this.timer = timer;
	}
}
