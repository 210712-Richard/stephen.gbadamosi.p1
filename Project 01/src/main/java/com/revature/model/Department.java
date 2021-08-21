package com.revature.model;

import java.util.HashMap;


public class Department {
	private Team name;
	private String deptHead;
	private HashMap<Integer, String> members;
	
	public Department() {
		name = Team.NONE;
		members = new HashMap<Integer, String>();
	}
	
	public Department(Team name) {
		this.name = name;
		members = new HashMap<Integer, String>();
	}
	
	public Department(Team name, Employee boss) {
		this.name = name;
		members.put(boss.getId(), boss.getUsername());
	}
	
	public Team getName() {
		return name;
	}
	public void setName(Team name) {
		this.name = name;
	}
	public String getDeptHead() {
		return deptHead;
	}

	public void setDeptHead(String deptHead) {
		this.deptHead = deptHead;
	}

	public HashMap<Integer, String> getMembers() {
		if(members == null) {
			members = new HashMap<>();
		}
		
		return members;
	}
	public void setMembers(HashMap<Integer, String> members) {
		this.members = members;
	}

	public String toString() {
		return Team.toString(this.name);
	}

}
