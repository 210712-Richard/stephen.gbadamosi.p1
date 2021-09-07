package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.revature.data.EmployeeDAOImpl;
import com.revature.model.Employee;
import com.revature.model.Role;


public class EmployeeServiceTest {

	private EmployeeService service;
		
	private Employee emp;
	
	@BeforeAll
	public static void beforeAll() {

	}

	@BeforeEach
	public void beforeEach() {		
		EmployeeDAOImpl empDao = Mockito.mock(EmployeeDAOImpl.class);
		emp = new Employee("test_user", "Testy Test", "test@testing.com", LocalDate.now(), Role.SUPERVISOR);
		service = new EmployeeService(empDao);
	}
	
	@Test
	public void testLoginValid() {
		ArgumentCaptor<String> usernameCaptor = ArgumentCaptor.forClass(String.class);
		service.login(emp.getUsername());
		
		Mockito.verify(service.empDao).search(usernameCaptor.capture());
		
		assertEquals(emp.getUsername(), usernameCaptor.getValue(),
				"Assert that the username passed in is the same username.");
	}
	
}
