package com.revature.model;

import java.util.ArrayList;
import java.util.List;


public class Department {
	private Team name;
	private String deptHead;
	private List<String> members;
	
	public Department() {
		name = Team.NONE;
		members = new ArrayList<String>();
	}
	
	public Department(Team name) {
		this.name = name;
		members = new ArrayList<String>();
	}
	
	public Department(Team name, String boss) {
		this.name = name;
		members.add(boss);
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

	public List<String> getMembers() {
		if(members == null) {
			members = new ArrayList<String>();
		}
		
		return members;
	}
	public void setMembers(List<String> members) {
		this.members = members;
	}

	public String toString() {
		return this.name.toString();
	}

}
