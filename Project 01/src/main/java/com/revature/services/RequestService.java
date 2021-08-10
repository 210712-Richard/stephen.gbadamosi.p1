package com.revature.services;

import java.util.List;
import java.util.UUID;

import com.revature.data.RequestDAOImpl;
import com.revature.model.Employee;
import com.revature.model.Request;

public class RequestService {
	public static RequestDAOImpl reqDao = new RequestDAOImpl();	
	
	public List<Request> viewRequests(Employee emp) {
		System.out.println("Returning request history for " + emp.getName());
		return reqDao.getAllUserRequest(emp.getUsername());
	}

	
	public Request searchRequest(UUID req_id) {
		// requests list should be populated from database
		
		Request req = null;
		if(RequestDAOImpl.requests != null && RequestDAOImpl.requests.size() > 0) {
			for(Request target : RequestDAOImpl.requests) {
				if(target.getReqID().equals(req_id)) {
					System.out.println("Found request in DB matching ID: " + req_id);
					return req;
				}
			}
		}
		
		return req;
	}
	
	public void updateRequest(Request req, Employee emp) {
		if(req == null || emp == null) {
			System.out.println("Invalid parameters for update request");
			return;
		}
		
		reqDao.update(req, emp);
	}
}
