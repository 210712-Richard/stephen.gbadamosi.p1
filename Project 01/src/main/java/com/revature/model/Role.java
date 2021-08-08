package com.revature.model;

public enum Role {
	FOUNDER, CEO, DEPARTMENT_HEAD, MANAGER, SUPERVISOR, COORDINATOR;
	
	public static String toString(Role name) {
		switch(name) {
		case FOUNDER:
			return "Founder";
		case CEO:
			return "CEO";
		case DEPARTMENT_HEAD:
			return "Head of Department";
		case MANAGER:
			return "Manager";
		case SUPERVISOR:
			return "Supervisor";
		case COORDINATOR:
			return "Coordinator";
		default:
			System.out.println("Invalid selection for Role type");
			return "N/A";
		}
	}
}
