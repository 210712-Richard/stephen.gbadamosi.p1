package com.revature.services;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import com.revature.data.EmployeeDAO;
import com.revature.data.EmployeeDAOImpl;
import com.revature.data.RequestDAOImpl;
import com.revature.model.Employee;
import com.revature.model.Request;

public class RequestService {
	public RequestDAOImpl reqDao;	
	
	public RequestService() {
		super();
		reqDao = new RequestDAOImpl();
	}
	
	public RequestService(RequestDAOImpl reqDao) {
		super();
		this.reqDao = reqDao;
	}
	
	public List<Request> viewRequests(Employee emp) {
		System.out.println("Returning request history for " + emp.getName());
		return reqDao.getAllUserRequest(emp.getUsername());
	}

	
	public Request searchRequest(UUID req_id) {		
		return reqDao.get(req_id);
	}
	
	public void setNextApprover(Request req, Employee emp) {
		if(req == null || emp == null) {
			System.out.println("Invalid input parameters passed to setNextApprover");
			return;
		}
		
		List<String> temp = new ArrayList<>(emp.getApprovalChain());
		System.out.println("Iterating through next approver list: ");
		String prev_approver = req.getNextApprover();
		
		for(int i = temp.size()-1; i >= 0; i--) {
			System.out.println(temp.get(i));
			if(temp.get(i).equalsIgnoreCase(prev_approver)) {
				if(i-1 >= 0) {
					System.out.println("Value of i: " + i);
					req.setNextApprover(temp.get(i-1));
				}
			}
		}		
	}
	
	public void updateRequest(Request req, Employee emp) {
		if(req == null || emp == null) {
			System.out.println("Invalid parameters for update request");
			return;
		}
		
		reqDao.update(req, emp);
//		tserv.startService(req);
	}
	
	// add request to approver's pending review list if it hasn't been added
	public void addRequest(UUID reqID, Employee emp) {
		EmployeeDAOImpl empDao = new EmployeeDAOImpl();

		if(emp == null || reqID == null) {
			System.out.println("Invalid parameters passed into addRequest function");
			return;
		}
		
		List<Request> temp = new ArrayList<>(emp.getPendingReview());
		System.out.println("Current list of requests pending review: " + temp.toString());
		for(Request r : temp) {
			if(r.getReqID().equals(reqID)) {
				System.out.println("Request already in Employee's review list");
				return;
			}
		}
		
		temp.add(reqDao.get(reqID));
		emp.setPendingReview(new LinkedList<Request>(temp));
		empDao.update(emp);
//		tserv.startService(searchRequest(reqID));
		System.out.println("Updated list of requests pending review: " + emp.getPendingReview().toString());
	}
	
	public void removeRequest(Request req, Employee emp) {
		EmployeeDAOImpl empDao = new EmployeeDAOImpl();
		if(emp == null || req == null) {
			System.out.println("Invalid parameters for remove Request - no operation performed");
			return;
		}
		List<Request> temp = new ArrayList<>(emp.getPendingReview());	
		System.out.println("Current list of requests pending review: " + temp.toString());
		for(int i = 0; i < emp.getPendingReview().size(); i++) {
			if(temp.get(i).getReqID().equals(req.getReqID())) {
				System.out.println("Found request in Employee's pending review list. Removing..");
				temp.remove(i);
				break;
			}
		}
		
		// if employee is requestor, add request to history if not already there
		boolean reqInList = false;
		for(Request r : emp.getHistory()) {
			if(r.getReqID().equals(req.getReqID())) {
				System.out.println("User history already contains request");
				reqInList = true;
				break;
			}
		}
		
		if(!reqInList) {
			emp.getHistory().add(req);
		}
				
		emp.setPendingReview(new LinkedList<Request>(temp));
		empDao.update(emp);
		System.out.println("Updated list of requests pending review: " + emp.getPendingReview().toString());

	}
}
