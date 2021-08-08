package com.revature.data;

import java.util.List;
import java.util.UUID;

import com.revature.model.Employee;
import com.revature.model.Request;

public interface RequestDAO {

	public void addRequest(Request req, Employee emp);
	
	public void updateRequest(Request req, Employee emp);
	
	public Request getRequest(UUID req_id);
	
	public List<Request> getAllUserRequest(String username);
	
	public List<Request> getAllRequests();
}
