package com.revature.services;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.util.UUID;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;

import com.revature.data.RequestDAOImpl;
import com.revature.model.Coverage;
import com.revature.model.Employee;
import com.revature.model.Request;
import com.revature.model.Role;

public class RequestServiceTest {

	private RequestService rservice;
	
	@Mock
	private RequestDAOImpl reqDao;
	
	private Employee emp;
	private Request req;
	
	@BeforeAll
	public static void beforeAll() {

	}

	@BeforeEach
	public void beforeEach() {
		reqDao = Mockito.mock(RequestDAOImpl.class);
		rservice = new RequestService(reqDao);
		emp = new Employee("test_user", "Testy Test", "test@testing.com", LocalDate.now(), Role.SUPERVISOR);
		req = new Request("Certification", Coverage.CERTIFICATION, emp.getUsername(), 500.00, LocalDate.of(2021, 8, 28));		
	}
	
	@Test
	public void testSearchRequest() {
		ArgumentCaptor<UUID> requestCaptor = ArgumentCaptor.forClass(UUID.class);

		Mockito.when(reqDao.get(req.getReqID()))
		.thenReturn(req);

		Request request = rservice.searchRequest(req.getReqID());
		
		Mockito.verify(reqDao).get(requestCaptor.capture());
		
		assertEquals(request.getReqID(), requestCaptor.getValue(),
				"Assert that the Request ID passed in is the same received.");

	}
}
