package com.revature.data;

import java.util.List;

import com.revature.model.Employee;
import com.revature.model.Team;

public interface EmployeeDAO {

	public void addEmployee(Employee emp, Team name);
	
	public void updateEmployee(Employee emp);
	
	public Employee getEmployee(String username);
	
//	public Employee getEmployeeByID(Integer eid);
	
	public List<Employee> getEmployees();
}
