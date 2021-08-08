package com.revature.model;

import java.util.ArrayList;
import java.util.List;


public class Department {
	private Team name;
	private List<Employee> members;
	
	public Department() {
		name = Team.NONE;
		members = new ArrayList<Employee>();
	}
	
	public Department(Team name) {
		this.name = name;
		members = new ArrayList<Employee>();
	}
	
	public Department(Team name, Employee boss) {
		this.name = name;
		members.add(boss);
	}
	
	public Team getName() {
		return name;
	}
	public void setName(Team name) {
		this.name = name;
	}
	public List<Employee> getMembers() {
		if(members == null) {
			members = new ArrayList<Employee>();
		}
		
		return members;
	}
	public void setMembers(List<Employee> members) {
		this.members = members;
	}

	public String toString() {
		return this.name.toString();
	}

}
