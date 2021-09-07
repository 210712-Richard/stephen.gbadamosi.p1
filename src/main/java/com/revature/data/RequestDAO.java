package com.revature.data;

import java.util.List;
import java.util.UUID;

import com.revature.model.Employee;
import com.revature.model.Request;

public interface RequestDAO {

	public void add(Request req, Employee emp);
	
	public void update(Request req, Employee emp);
	
	public Request get(UUID req_id);
	
	public List<Request> getAllUserRequest(String username);
	
	public List<Request> getAllRequests();
}
