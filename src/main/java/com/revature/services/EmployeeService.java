package com.revature.services;

import com.revature.data.EmployeeDAOImpl;
import com.revature.model.Employee;

public class EmployeeService {
	public EmployeeDAOImpl empDao;
	
	public EmployeeService() {
		super();
		EmployeeDAOImpl empDao = new EmployeeDAOImpl();
	}
	
	public EmployeeService(EmployeeDAOImpl empDao) {
		this.empDao = empDao;
	}
	
	public Employee login(String username) {
		return empDao.search(username);
	}	
	
	public Employee searchEmployees(String username) {
		if(username == null || username.equals("")) {
			System.out.println("Invalid parameters passed into Employee search");
			return null;
		}
		
		return empDao.search(username);
		
	}
	
	// Search employee approval chain for username supplied
	public boolean scanApprovalChain(Employee emp, String username) {
		if(emp == null || username == null || username.equals("")) {
			System.out.println("Invalid search parameters, search aborted");
			return false;
		}
		
		for(String ap : emp.getApprovalChain()) {
			if(ap.equals(username)) {
				System.out.println(username + " found in " + emp.getUsername() + "'s approval chain");
				return true;
			}
		}
		
		return false;
	}
	
	public void updateEmployee(Employee emp) {
		if(emp == null) {
			System.out.println("Invalid parameters passed to update Employee");
			return;
		}
		
		empDao.update(emp);
	}
	
}
