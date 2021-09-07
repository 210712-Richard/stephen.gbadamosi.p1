package com.revature.data;

import java.time.LocalDate;
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
	
	public static List<Request> requests;
	public static List<Request> pending;
	
	@Override
	public void add(Request req, Employee emp) {
		EmployeeDAOImpl edao = new EmployeeDAOImpl();
		if(requests == null) 
			requests = new ArrayList<>();
		if(pending == null)
			pending = new ArrayList<>();
		
		System.out.println("Local requests size is " + requests.size());
		System.out.println("Pending requests size is " + pending.size());
		String query = "";

		// if event date has passed, reject add request
		if(req.getEventDate().isBefore(LocalDate.now().minusDays(1))) {
			System.out.println("Too late to submit a reimbursement for event date without manager approval");
			System.out.println("Unable to add request");
			return;
		}
		
		// Check if request exists in DB already
		for(Request request : requests) {
			if(request.equals(req)) {
				if(request.getStatus() == Status.APPROVED) {
					System.out.println("Existing request by description already approved");
					return;
				}
				
				System.out.println("Existing request with ID: " + req.getReqID() + " pending approval. Use update to modify request");
				System.out.println("Next approver: " + request.getNextApprover());
				sortRequests();
				return;
			}
		}

		System.out.println("Adding new request for " + emp.getUsername());
		
		if(emp.getApprovalChain().empty())
			edao.configureApprovalChain(emp);
		
		System.out.println("Next approver for request is: " + emp.getApprovalChain().peek());
		Employee approver = edao.search(emp.getApprovalChain().peek());
		System.out.println("Next approver: " + approver.getUsername());
		req.setNextApprover(approver.getUsername());
		emp.getHistory().add(req);
		approver.getPendingReview().add(req);
		calculateReimbursementAmount(req, emp);
		List<UUID> req_list = transformRequest(emp.getHistory());
		edao.update(emp);
		edao.update(approver);
		
		System.out.println("Request list transformed to UUID list in add: " + req_list.toString());
		
		query = "Insert into request (req_id, description, type, requestor, next_approver, cost, reimburse_amount, docs, requestees, passing_grade, "
				+ "event_date, submission_date, status, priority, comment, commHistory) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(req.getReqID(), req.getDescription(), req.getType().toString(), emp.getUsername(), req.getNextApprover(), req.getCost(), req.getReimburseAmount(), req.getDocs(), req.getRequestees(),
						 req.getPassingGrade(), req.getEventDate(), req.getSubmittedDate(), req.getStatus().toString(), req.getSLA().toString(), req.getComment(), req.getCommHistory().toString());
		session.execute(bound);
				
		System.out.println("Request with ID: " + req.getReqID() + " has been successfully added to reimbursement requests database");
		System.out.println(emp.getName() + " request history updated: " + emp.toString());
		sortRequests();
	}

	@Override
	public void update(Request req, Employee emp) {

		String query = "Update request Set description = ?, next_approver = ?, cost = ?, reimburse_amount = ?, docs = ?, requestees = ?, passing_grade = ?, "
				+ "event_date = ?, submission_date = ?, status = ?, priority = ?, comment = ?, commHistory = ? where req_id = ? and requestor = ?;";
		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(req.getDescription(), req.getNextApprover(), req.getCost(), req.getReimburseAmount(), req.getDocs(), req.getRequestees(), req.getPassingGrade(), req.getEventDate(),
						req.getSubmittedDate(), req.getStatus().toString(), req.getSLA().toString(), req.getComment(), req.getCommHistory().toString(), req.getReqID(), emp.getUsername());
		session.execute(bound);
		
		System.out.println("Request updated successfully");
		requests = getAllRequests();
		sortRequests();

	}

	@Override
	public Request get(UUID req_id) {
		// requests list should be populated from database
		
		// debug
		System.out.println("Searching for request ID: " + req_id);
		System.out.println("Local requests size: " + RequestDAOImpl.requests.size());

		if(RequestDAOImpl.requests != null && RequestDAOImpl.requests.size() > 0) {
			for(Request target : RequestDAOImpl.requests) {
				if(target.getReqID().equals(req_id)) {
					System.out.println("Found request in DB matching ID: " + req_id);
					return target;
				}
			}
		}
		
		String query = "Select description, type, requestor, next_approver, cost, reimburse_amount, docs, requestees, passing_grade, event_date, submission_date, "
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
		req.setReqID(req_id);
		req.setDescription(row.getString("description"));
		req.setType(Coverage.getCoverage(row.getString("type")));
		req.setRequestor(row.getString("requestor"));
		req.setNextApprover(row.getString("next_approver"));
		req.setCost(row.getDouble("cost"));
		req.setDocs(row.getList("docs", String.class));
		req.setRequestees(row.getList("requestees", String.class));
		req.setReimburseAmount(row.getDouble("reimburse_amount"));
		req.setEventDate(row.getLocalDate("event_date"));
		req.setSubmittedDate(row.getLocalDate("submission_date"));
		req.setStatus(Status.valueOf(row.getString("status")));
		req.setSLA(Priority.getPriority(row.getString("priority")));
		req.setComment(row.getString("comment"));
		req.setCommHistory(new StringBuilder(row.getString("commHistory")));
		
		return req;
	}

	@Override
	public List<Request> getAllUserRequest(String username) {
		EmployeeDAOImpl edao = new EmployeeDAOImpl();
		Employee emp = edao.search(username);
		String query = "Select req_id, description, type, requestor, next_approver, cost, reimburse_amount, docs, passing_grade, event_date, submission_date, "
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
			req.setType(Coverage.getCoverage(row.getString("type")));
			req.setRequestor(row.getString("requestor"));
			req.setNextApprover(row.getString("next_approver"));
			req.setCost(row.getDouble("cost"));
			req.setDocs(row.getList("docs", String.class));
			req.setRequestees(row.getList("requestees", String.class));
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
		String query = "Select req_id, description, type, requestor, next_approver, cost, reimburse_amount, docs, passing_grade, event_date, submission_date, "
				+ "status, priority, comment, commHistory from request;";
		SimpleStatement s = new SimpleStatementBuilder(query).build();

		ResultSet rs = session.execute(s);

		if(rs == null) {
			System.out.println("No requests found in database.");
			// if there are no return values
			return null;
		}
		
		requests = new ArrayList<>();
		
		rs.forEach(row -> {	
			Request req = new Request();
			req.setReqID(row.getUuid("req_id"));
			req.setDescription(row.getString("description"));
			req.setType(Coverage.getCoverage(row.getString("type")));
			req.setRequestor(row.getString("requestor"));
			req.setNextApprover(row.getString("next_approver"));
			req.setCost(row.getDouble("cost"));
			req.setDocs(row.getList("docs", String.class));
	//		req.setRequestees(row.getList("requestees", String.class));
			req.setReimburseAmount(row.getDouble("reimburse_amount"));
			req.setEventDate(row.getLocalDate("event_date"));
			req.setSubmittedDate(row.getLocalDate("submission_date"));
			req.setStatus(Status.valueOf(row.getString("status")));
			req.setSLA(Priority.getPriority(row.getString("priority")));
			req.setComment(row.getString("comment"));
			req.setCommHistory(new StringBuilder(row.getString("commHistory")));
			
			requests.add(req);
		});
		
			sortRequests();
			System.out.println("Found a total of " + requests.size() + " requests in DB");
			return requests;
	}
	
	public static void sortRequests() {
		// Adds non approved requests to pending list
		if(pending == null)
			pending = new ArrayList<>();
				
		for(Request r : requests) {
			if(r.getStatus() != Status.APPROVED && r.getStatus() != Status.DENIED
					&& !pending.contains(r))
				pending.add(r);
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
	
	// Modifies reimbursement balance and amount if reimbursement balance is greater than 0
	public static void calculateReimbursementAmount(Request req, Employee emp) {
		
		if(emp.getReimburseBalance() <= 0.0)
			System.out.println(emp.getLastRenewal().getDayOfYear() + " Reimbursement allowance for " + emp.getUsername() + " exhausted."
					+ "\nBalance will refresh next year");
		
		Double amount = req.getType().getValue() * req.getCost();
		System.out.println("Reimbursement amount for this request is: " + amount);
		
		Double balance = emp.getReimburseBalance();
		Double total = amount;
		
		// Include sum of pending reimbursements in total
		for(Request r : emp.getHistory()) {
			if(r.getStatus() != Status.APPROVED || r.getStatus() != Status.APPEAL && r.getReqID() != req.getReqID()) {
				total = total + r.getReimburseAmount();
			}
		}
		
		System.out.println("Reimbursement Total for " + emp.getUsername() + " is: " + total);
		System.out.println("Meanwhile reimbursement balance is: " + balance);
		
		if(total > balance) {
			System.out.println("WARNING: The reimbursement amount of all requests for this employee exceeds reimbursement balance");
		}
		
		req.setReimburseAmount(amount);
		
	}
	
}
