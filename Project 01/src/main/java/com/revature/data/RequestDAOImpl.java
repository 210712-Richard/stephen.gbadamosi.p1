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
import com.revature.util.CassandraUtil;

public class RequestDAOImpl implements RequestDAO {
	private CqlSession session = CassandraUtil.getInstance().getSession();
	private static List<Request> requests;
	private static List<Request> pending;

	@Override
	public void addRequest(Request req, Employee emp) {
		
		String query = "Insert into request (reqId, description, type, requestor, cost, reimburse_amount, docsURL, passing_grade, "
				+ "event_date, submission_date, status, priority, comment, commHistory) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(req.getReqID(), req.getDescription(), req.getType().toString(), emp.getUsername(), req.getCost(), req.getReimburseAmount(), req.getDocsURL(),
						 req.getPassingGrade(), req.getEventDate(), req.getSubmittedDate(), req.getStatus().toString(), req.getSLA(), req.getComment(), req.getCommHistory());
		session.execute(bound);
		
		System.out.println("Adding new request for " + emp.getUsername());
		requests.add(req); 	// review: may not be necessary
		pending.add(req); 	// New request so automatically added to pending and manager's queue
		
		System.out.println("Request with ID: " + req.getReqID() + " has been successfully added to reimbursement requests database");
	}

	@Override
	public void updateRequest(Request req, Employee emp) {

		String query = "Update request description = ?, requestor = ?, cost = ?, reimburse_amount = ?, docsURL = ?, passing_grade = ?, "
				+ "event_date = ?, submission_date = ?, status = ?, priority = ?, comment = ?, commHistory = ? where req_id = ? and requestor = ?;";
		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(req.getDescription(), emp.getUsername(), req.getCost(), req.getReimburseAmount(), req.getDocsURL(), req.getPassingGrade(), req.getEventDate(),
						req.getSubmittedDate(), req.getStatus().toString(), req.getSLA(), req.getComment(), req.getCommHistory(), req.getReqID(), emp.getUsername());
		session.execute(bound);
		
		System.out.println("Request updated successfully");

	}

	@Override
	public Request getRequest(UUID req_id) {
		String query = "Select description, type, requestor, cost, reimburse_amount, docsURL, passing_grade, event_date, submission_date, "
				+ "status, priority, comment, commHistory where req_id = ? and requestor = ;";
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
		req.setDescription(row.getString("description"));
		req.setType(Coverage.valueOf(row.getString("type")));
		req.setRequestor(EmployeeDAOImpl.searchEmployees((row.getString("requestor"))));
		req.setCost(row.getDouble("cost"));
		req.setDocsURL(row.getString("DocsURL"));
		req.setReimburseAmount(row.getDouble("reimburse_amount"));
		req.setEventDate(row.getLocalDate("event_date"));
		req.setSubmittedDate(row.getLocalDate("submission_date"));
		req.setStatus(Status.valueOf(row.getString("status")));
		req.setSLA(Priority.valueOf(row.getString("priority")));
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
		
		return null;
	}

	@Override
	public List<Request> getAllRequests() {
		String query = "Select reqID, description, type, requestor, cost, reimburse_amount, docsURL, passing_grade, event_date, submission_date, "
				+ "status, priority, comment, commHistory;";
		SimpleStatement s = new SimpleStatementBuilder(query).build();

		ResultSet rs = session.execute(s);

		if(rs == null) {
			System.out.println("No requests found in database.");
			// if there are no return values
			return null;
		}
		
		rs.forEach(row -> {	
			Request req = new Request();
			req.setReqID(row.getUuid("reqID"));
			req.setDescription(row.getString("description"));
			req.setType(Coverage.valueOf(row.getString("type")));
			req.setRequestor(EmployeeDAOImpl.searchEmployees((row.getString("requestor"))));
			req.setCost(row.getDouble("cost"));
			req.setDocsURL(row.getString("DocsURL"));
			req.setReimburseAmount(row.getDouble("reimburse_amount"));
			req.setEventDate(row.getLocalDate("event_date"));
			req.setSubmittedDate(row.getLocalDate("submission_date"));
			req.setStatus(Status.valueOf(row.getString("status")));
			req.setSLA(Priority.valueOf(row.getString("priority")));
			req.setComment(row.getString("comment"));
			req.setCommHistory(new StringBuilder(row.getString("commHistory")));
			
			requests.add(req);
		});
			
			return requests;
	}
	
	public static List<UUID> transformRequest(List<Request> req_list) {
		List<UUID> uid_list = null;
		
		if(req_list != null && req_list.size() > 0) {
			uid_list = new ArrayList<>();
			for(Request req : req_list) {
				uid_list.add(req.getReqID());
				System.out.println("Transformed Request object with ID: " + req.getReqID());
			}
		}
		
		return uid_list;
	}
	
	public Request searchRequest(UUID req_id) {
		// requests list should be populated from database
		
		Request req = null;
		if(requests != null && requests.size() > 0) {
			for(Request target : requests) {
				if(target.getReqID().equals(req_id)) {
					System.out.println("Found request in DB matching ID: " + req_id);
					return req;
				}
			}
		}
		
		return req;
	}
	
	public static void checkStatus(Request req) {
		// Check status of request and route to managers or benCo for approval if necessary
		
	}

}
