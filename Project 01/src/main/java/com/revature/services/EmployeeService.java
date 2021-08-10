package com.revature.services;

import java.util.ArrayList;
import java.util.List;

import com.revature.data.EmployeeDAOImpl;
import com.revature.model.Employee;
import com.revature.model.Request;

public class EmployeeService {
	public static EmployeeDAOImpl empDao = new EmployeeDAOImpl();
	
	
	public Employee login(String username) {
		return empDao.searchEmployees(username);
	}	
	
}
