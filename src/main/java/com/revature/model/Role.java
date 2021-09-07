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
	
	public static Role getValue(String role) {
		if(role.equalsIgnoreCase("Founder"))
			return FOUNDER;
		if(role.equalsIgnoreCase("CEO"))
			return CEO;
		if(role.equalsIgnoreCase("Head of Department"))
			return DEPARTMENT_HEAD;
		if(role.equalsIgnoreCase("Manager"))
			return MANAGER;
		if(role.equalsIgnoreCase("Supervisor"))
			return SUPERVISOR;
		if(role.equalsIgnoreCase("Coordinator"))
			return COORDINATOR;
		
		return null;
		
	}
}
