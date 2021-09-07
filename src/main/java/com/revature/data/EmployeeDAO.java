package com.revature.data;

import java.util.List;

import com.revature.model.Employee;
import com.revature.model.Team;

public interface EmployeeDAO {

	public void add(Employee emp, Team name);
	
	public void update(Employee emp);
	
	public Employee search(String username);
	
	public Employee get(String username);
		
	public List<Employee> getEmployees();
}
