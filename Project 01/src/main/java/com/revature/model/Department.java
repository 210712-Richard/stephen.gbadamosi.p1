package com.revature.model;

import java.util.ArrayList;
import java.util.List;


public class Department {
	private String name;
	private List<Employee> members;
	
	public Department(String name) {
		this.name = name;
	}
	
	public Department(String name, Employee boss) {
		this.name = name;
		members.add(boss);
	}
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
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


}
