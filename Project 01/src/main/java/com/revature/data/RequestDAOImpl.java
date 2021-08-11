package com.revature.data;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.revature.model.Coverage;
import com.revature.model.Employee;
import com.revature.model.Priority;
import com.revature.model.Request;
import com.revature.model.Status;
import com.revature.services.EmployeeService;
import com.revature.util.CassandraUtil;

public class RequestDAOImpl implements RequestDAO {
	private CqlSession session = CassandraUtil.getInstance().getSession();
	private EmployeeDAOImpl edao = new EmployeeDAOImpl();
	public static List<Request> requests;
	public static List<Request> pending;

	static {
		if(requests == null)
			requests = new ArrayList<>();

		
		if(pending == null)
			pending = new ArrayList<>();
			// populate from requests

	}
	
	{
		// populate requests from database 
		getAllRequests();
		
		// sort requests by status
		sortRequests();
	}
	
	@Override
	public void add(Request req, Employee emp) {
		System.out.println("Local requests size is " + requests.size());
		String query = "";

		// Check if request exists in DB already
		for(Request request : requests) {
			System.out.println("Existing Request: " + request.toString());
			System.out.println("Potentially new Request: " + req.toString());
			System.out.println("Are they equal? (" + request.equals(req) + ")");
			if(request.equals(req)) {
				if(request.getStatus() == Status.APPROVED) {
					System.out.println("Existing request by description already approved");
					return;
				}
				
				System.out.println("Existing request by description pending approval. Use update to modify request");
				
//					query = "Update request Set description = ?, cost = ?, reimburse_amount = ?, docs = ?, passing_grade = ?, "
//							+ "event_date = ?, submission_date = ?, status = ?, priority = ?, comment = ?, commHistory = ? where req_id = ? and requestor = ?;";
//					SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
//					BoundStatement bound = session.prepare(s)
//							.bind(req.getDescription(), req.getCost(), req.getReimburseAmount(), req.getDocs(), req.getPassingGrade(), req.getEventDate(),
//									req.getSubmittedDate(), req.getStatus().toString(), req.getSLA().toString(), req.getComment(), req.getCommHistory().toString(), request.getReqID(), emp.getUsername());
//					session.execute(bound);
//					
//					System.out.println("Request updated successfully");
				return;
			}
		}
		
		if(requests == null) {
			requests = new ArrayList<>();
		}
		
		query = "Insert into request (req_id, description, type, requestor, cost, reimburse_amount, docs, passing_grade, "
				+ "event_date, submission_date, status, priority, comment, commHistory) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(req.getReqID(), req.getDescription(), req.getType().toString(), emp.getUsername(), req.getCost(), req.getReimburseAmount(), req.getDocs(),
						 req.getPassingGrade(), req.getEventDate(), req.getSubmittedDate(), req.getStatus().toString(), req.getSLA().toString(), req.getComment(), req.getCommHistory().toString());
		session.execute(bound);
		
		System.out.println("Adding new request for " + emp.getUsername());
		requests.add(req); 	// review: may not be necessary
		pending.add(req); 	// New request so automatically added to pending and manager's queue
		emp.getHistory().add(req);
		
		System.out.println("Request with ID: " + req.getReqID() + " has been successfully added to reimbursement requests database");
		System.out.println(emp.getName() + " request history updated: " + emp.toString());
	}

	@Override
	public void update(Request req, Employee emp) {

		String query = "Update request Set description = ?, cost = ?, reimburse_amount = ?, docs = ?, passing_grade = ?, "
				+ "event_date = ?, submission_date = ?, status = ?, priority = ?, comment = ?, commHistory = ? where req_id = ? and requestor = ?;";
		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(req.getDescription(), req.getCost(), req.getReimburseAmount(), req.getDocs(), req.getPassingGrade(), req.getEventDate(),
						req.getSubmittedDate(), req.getStatus().toString(), req.getSLA().toString(), req.getComment(), req.getCommHistory().toString(), req.getReqID(), emp.getUsername());
		session.execute(bound);
		
		System.out.println("Request updated successfully");

	}

	@Override
	public Request get(UUID req_id) {
		String query = "Select description, type, requestor, cost, reimburse_amount, docs, passing_grade, event_date, submission_date, "
				+ "status, priority, comment, commHistory from request where req_id = ?;";
		SimpleStatement s = new SimpleStatementBuilder(query).build();
		BoundStatement bound = session.prepare(s).bind(req_id);

		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		
		if(row == null) {
			System.out.println("No such request found in database. Request ID: " + req_id);
			// if there are no return values
			return null;
		}
		

		Request req = new Request();
		req.setReqID(row.getUuid("req_id"));
		req.setDescription(row.getString("description"));
		req.setType(Coverage.valueOf(row.getString("type")));
		req.setRequestor(edao.searchEmployees((row.getString("requestor"))));
		req.setCost(row.getDouble("cost"));
		req.setDocs(row.getList("docs", String.class));
		req.setReimburseAmount(row.getDouble("reimburse_amount"));
		req.setEventDate(row.getLocalDate("event_date"));
		req.setSubmittedDate(row.getLocalDate("submission_date"));
		req.setStatus(Status.valueOf(row.getString("status")));
		req.setSLA(Priority.getPriority(row.getString("priority")));
		req.setComment(row.getString("comment"));
		req.setCommHistory(new StringBuilder(row.getString("commHistory")));
////	row = rs.one();
////	if(row != null)
////		throw new RuntimeException("More than one employee with same username");
////
		
		return req;
	}

	@Override
	public List<Request> getAllUserRequest(String username) {
		Employee emp = edao.searchEmployees(username);
		String query = "Select req_id, description, type, requestor, cost, reimburse_amount, docs, passing_grade, event_date, submission_date, "
				+ "status, priority, comment, commHistory from request where username = ?;";
		SimpleStatement s = new SimpleStatementBuilder(query).build();
		BoundStatement bound = session.prepare(s).bind(username);

		ResultSet rs = session.execute(bound);

		if(rs == null) {
			System.out.println("No requests found in database.");
			// if there are no return values
			return null;
		}
		
		rs.forEach(row -> {	
			Request req = new Request();
			req.setReqID(row.getUuid("req_id"));
			req.setDescription(row.getString("description"));
			req.setType(Coverage.valueOf(row.getString("type")));
			req.setRequestor(edao.searchEmployees((row.getString("requestor"))));
			req.setCost(row.getDouble("cost"));
			req.setDocs(row.getList("docs", String.class));
			req.setReimburseAmount(row.getDouble("reimburse_amount"));
			req.setEventDate(row.getLocalDate("event_date"));
			req.setSubmittedDate(row.getLocalDate("submission_date"));
			req.setStatus(Status.valueOf(row.getString("status")));
			req.setSLA(Priority.getPriority(row.getString("priority")));
			req.setComment(row.getString("comment"));
			req.setCommHistory(new StringBuilder(row.getString("commHistory")));
			
			emp.getHistory().add(req);
		});
			
			return emp.getHistory();
	}

	@Override
	public List<Request> getAllRequests() {
		System.out.println("Getting all requests from DB");
		String query = "Select req_id, description, type, requestor, cost, reimburse_amount, docs, passing_grade, event_date, submission_date, "
				+ "status, priority, comment, commHistory from request;";
		SimpleStatement s = new SimpleStatementBuilder(query).build();

		ResultSet rs = session.execute(s);

		if(rs == null) {
			System.out.println("No requests found in database.");
			// if there are no return values
			return null;
		}
		
//		requests = new ArrayList<>();
		
		rs.forEach(row -> {	
			Request req = new Request();
			req.setReqID(row.getUuid("req_id"));
			req.setDescription(row.getString("description"));
			req.setType(Coverage.valueOf(row.getString("type")));
			req.setRequestor(edao.searchEmployees((row.getString("requestor"))));
			req.setCost(row.getDouble("cost"));
			req.setDocs(row.getList("docs", String.class));
			req.setReimburseAmount(row.getDouble("reimburse_amount"));
			req.setEventDate(row.getLocalDate("event_date"));
			req.setSubmittedDate(row.getLocalDate("submission_date"));
			req.setStatus(Status.valueOf(row.getString("status")));
			req.setSLA(Priority.getPriority(row.getString("priority")));
			req.setComment(row.getString("comment"));
			req.setCommHistory(new StringBuilder(row.getString("commHistory")));
			
			requests.add(req);
		});
			System.out.println("Found a total of " + requests.size() + " requests in DB");
			return requests;
	}
	
	public void sortRequests() {
		// Adds non approved requests to pending list
		if(requests.size() > 0) {
			for(Request r : requests) {
				if(r.getStatus() != Status.APPROVED)
					pending.add(r);
			}
		}
		
	}
	
	public static List<UUID> transformRequest(List<Request> req_list) {
		List<UUID> uid_list = new ArrayList<>();
		
		if(req_list == null || req_list.size() == 0) {
			System.out.println("Input list is empty - no transform operation performed");
			return uid_list;
		}
		
		else {
			uid_list = new ArrayList<>();
			for(Request req : req_list) {
				uid_list.add(req.getReqID());
				System.out.println("Transformed Request object with ID: " + req.getReqID());
			}
		}
		
		return uid_list;
	}
	
	public List<Request> revertRequest(List<UUID> requestID_list) {
		List<Request> req_list = new ArrayList<>();
		System.out.println("Request List from DB to transform:\n" + requestID_list);
		
		if(requestID_list == null || requestID_list.size() == 0) {
			System.out.println("Input list is empty - no transform operation performed");
			return req_list;
		}
		
		else {			
			for(UUID id : requestID_list) {
				Request new_req = get(id);
				if(new_req == null) {
					continue;
				}
				else {
					req_list.add(new_req);
					System.out.println("Created new request object from ID: " + new_req.getReqID());
				}
			}
		}
		
		return req_list;
	}

	
	public static void checkStatus(Request req) {
		// Check status of request and route to managers or benCo for approval if necessary
		
	}
	
}
