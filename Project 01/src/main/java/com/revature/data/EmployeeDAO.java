package com.revature.data;

import java.util.List;

import com.revature.model.Employee;
import com.revature.model.Team;

public interface EmployeeDAO {

	public void add(Employee emp, Team name);
	
	public void update(Employee emp);
	
	public Employee get(String username);
	
//	public Employee getEmployeeByID(Integer eid);
	
	public List<Employee> getEmployees();
}
