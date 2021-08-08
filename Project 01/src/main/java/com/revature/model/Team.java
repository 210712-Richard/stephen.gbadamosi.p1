package com.revature.model;

public enum Team {
	ALL, BOARD_OF_DIRECTORS, ACCOUNTING, ENGINEERING, IT, BENEFITS, NONE;
	
	
	public String toString(Team name) {
		switch(name) {
		case ALL:
			return "All";
		case ACCOUNTING:
			return "Accounting";
		case ENGINEERING:
			return "Engineering";
		case IT:
			return "IT";
		case BENEFITS:
			return "Benefits";
		case NONE:
		default:
			System.out.println("Invalid selection for Team type");
			return "N/A";
		}
	}
	
//	public String valueOf(String name) {
//		switch(name) {
//		case "All":
//			return ALL;
//		case "Accounting":
//			return ACCOUNTING;
//		case "Engineering":
//			return ENGINEERING;
//		case "IT":
//			return IT;
//		case "Benefits":
//			return BENEFITS;
//		case "N/A":
//		default:
//			System.out.println("Invalid selection for Team type");
//			return NONE;
//		}
//	}
}
