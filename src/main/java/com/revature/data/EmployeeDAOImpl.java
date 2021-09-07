package com.revature.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;
import java.util.UUID;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.DefaultConsistencyLevel;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatementBuilder;
import com.revature.model.Department;
import com.revature.model.Employee;
import com.revature.model.Request;
import com.revature.model.Role;
import com.revature.model.Team;
import com.revature.util.CassandraUtil;

public class EmployeeDAOImpl implements EmployeeDAO {
	private CqlSession session;
	public RequestDAOImpl reqDao;
	public static HashMap<Integer, Employee> employees;
	public static List<Department> departments;


	static {
		if(employees == null) {
			employees = new HashMap<>();
		}
		if(departments == null) {
			departments = new ArrayList<>();
			departments.add(new Department(Team.ALL));
			departments.add(new Department(Team.BOARD_OF_DIRECTORS));
			departments.add(new Department(Team.ACCOUNTING));
			departments.add(new Department(Team.ENGINEERING));
			departments.add(new Department(Team.IT));
			departments.add(new Department(Team.BENEFITS));
			departments.add(new Department(Team.NONE));
		}
	}
	
	public EmployeeDAOImpl() {
		session = CassandraUtil.getInstance().getSession();
		reqDao =  new RequestDAOImpl();
	
	}
	
	public EmployeeDAOImpl(CqlSession session, RequestDAOImpl reqDao, HashMap<Integer, Employee> emps, List<Department> depts) {
		this.session = session;
		this.reqDao =  reqDao;
		this.employees = emps;
		this.departments = depts;
	}
	@Override
	public void add(Employee emp, Team name) {
//		if(employees == null) {
//			employees = new HashMap<>();
//		}
	// Set new employee department before add
		configureDept(emp, name);
		String manager = emp.getManager() == null ? "None" : emp.getManager();
		emp.setManager(manager);
		
		String query;
		if((search(emp.getUsername())) != null) {
			System.out.println("Employee already in DB. Use update to modify account details");
			return;
		}
		
		query = "Insert into employee (id, username, name, email, message, birthday, department, role, manager, "
				+ "approvalchain, pendingReview, history, reimburserecvd, reimbursebal, lastRenewal) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(emp.getId(), emp.getUsername(), emp.getName(), emp.getEmail(), emp.getMessage(), emp.getBirthday(), Team.toString(emp.getDept().getName()), Role.toString(emp.getRole()),
						 emp.getManager(), new ArrayList<String>(emp.getApprovalChain()), qToList(emp.getPendingReview()), RequestDAOImpl.transformRequest(emp.getHistory()), emp.getReimburseRecvd(), emp.getReimburseBalance(), emp.getLastRenewal());
		session.execute(bound);
		
		employees.put(emp.getId(), emp); 
		System.out.println("Adding employee to Team: " + name);
		emp.getDept().getMembers().put(emp.getId(), emp.getUsername());
		
		System.out.println(emp.getName() + " has been successfully added to company database");
	}

	@Override
	public void update(Employee emp) {
		String query = "Update employee Set name = ?, email = ?, message = ?, birthday = ?, department = ?, role = ?, manager = ?, " +
		"approvalchain = ?, pendingreview = ?, history = ?, reimburserecvd = ?, reimbursebal = ?, lastrenewal = ? where username = ?;";

		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(emp.getName(), emp.getEmail(), emp.getMessage(), emp.getBirthday(), Team.toString(emp.getDept().getName()), Role.toString(emp.getRole()), emp.getManager(),
						new ArrayList<String>(emp.getApprovalChain()), qToList(emp.getPendingReview()), RequestDAOImpl.transformRequest(emp.getHistory()), emp.getReimburseRecvd(), emp.getReimburseBalance(), emp.getLastRenewal(), emp.getUsername());
		session.execute(bound);
		String apostrophe = emp.getUsername().substring(emp.getUsername().length()-1).equals("s") ? "'" : "'s";
		System.out.println(emp.getUsername() + apostrophe + " account details updated successfully");
	}

	@Override
	public Employee get(String username) {
		String query = "Select id, username, name, email, message, birthday, department, role, manager, approvalchain, "
		+ "pendingReview, history, reimburserecvd, reimbursebal, lastrenewal from employee where username = ?";
		SimpleStatement s = new SimpleStatementBuilder(query).build();
		BoundStatement bound = session.prepare(s).bind(username);

		ResultSet rs = session.execute(bound);
		Row row = rs.one();
	
		if(row == null) {
			System.out.println("No employee registered in database with username: " + username);
			// if there are no return values
			return null;
		}
		
		Employee emp = new Employee(username);
		emp.setId(row.getInt("id"));
		emp.setName(row.getString("name"));
		emp.setUsername(row.getString("username"));
		emp.setEmail(row.getString("email"));
		emp.setMessage(row.getString("message"));
		emp.setBirthday(row.getLocalDate("birthday"));
		List<UUID> history = row.getList("history", UUID.class);
		System.out.println("Submitted Requests: " + history);
		if(history != null && history.size() > 0)
			emp.setHistory(reqDao.revertRequest(history));
		
		else {
			List<Request> new_list = new ArrayList<Request>();
			emp.setHistory(new_list);
		}
		
		Stack<String> approval_chain = new Stack<String>();
		approval_chain.addAll(new ArrayList<>(row.getList("approvalchain", String.class)));
		emp.setApprovalChain(approval_chain);
		
		List<UUID> review = row.getList("pendingreview", UUID.class);
		Queue<Request> new_review = new LinkedList<Request>();
		System.out.println("Pending review requests: " + review);
		if(review != null && review.size() > 0) {
			new_review.addAll(reqDao.revertRequest(review));
			emp.setPendingReview(new_review);
			
		}
		
		else {
			Queue<Request> new_q = new LinkedList<Request>();
			emp.setPendingReview(new_q);
		}
		emp.setDept(searchDept(Team.getTeam(row.getString("department"))));
		emp.setRole(Role.getValue(row.getString("role")));
		emp.setManager(row.getString("manager"));
		emp.setReimburseRecvd(row.getDouble("reimburserecvd"));
		emp.setReimburseBalance(row.getDouble("reimbursebal"));
		emp.setLastRenewal(row.getLocalDate("lastrenewal"));
		
		return emp;
	}

	@Override
	public List<Employee> getEmployees() {
		
		String query = "Select id, username, name, email, message, birthday, department, role, manager, "
		+ "pendingReview, history, reimburseRecvd, reimburseBal, lastRenewal from employee";
		SimpleStatement s = new SimpleStatementBuilder(query).build();

		ResultSet rs = session.execute(s);
		if(rs.iterator() == null) {
			System.out.println("No employees currrently registered in database");
			// if there are no return values
			return null;
		}
		
		employees = new HashMap<>();	
		
		rs.forEach(row -> {	
			String username = row.getString("username");
			Employee emp = new Employee(username);
			emp.setId(row.getInt("id"));
			emp.setName(row.getString("name"));
			emp.setEmail(row.getString("email"));
			emp.setMessage(row.getString("message"));
			emp.setBirthday(row.getLocalDate("birthday"));
			List<UUID> history = row.getList("history", UUID.class);
			if(history != null && history.size() > 0) {
				System.out.println(history);
				emp.setHistory(reqDao.revertRequest(history));
			}
			
			else {
				List<Request> new_list = new ArrayList<Request>();
				emp.setHistory(new_list);
			}
			
			
			Stack<String> approval_chain = new Stack<String>();
			approval_chain.addAll(new ArrayList<>(row.getList("approvalchain", String.class)));
			emp.setApprovalChain(approval_chain);
			
			List<UUID> review = row.getList("pendingreview", UUID.class);
			Queue<Request> new_review = new LinkedList<Request>();
			System.out.println("Pending review requests: " + review);
			if(review != null && review.size() > 0) {
				new_review.addAll(reqDao.revertRequest(review));
				emp.setPendingReview(new_review);
				
			}
			
			else {
				Queue<Request> new_q = new LinkedList<Request>();
				emp.setPendingReview(new_q);
			}
			
			emp.setDept(searchDept(Team.getTeam(row.getString("department"))));
			emp.setRole(Role.getValue(row.getString("role")));
			emp.setManager(row.getString("manager"));
			emp.setReimburseRecvd(row.getDouble("reimburserecvd"));
			emp.setReimburseBalance(row.getDouble("reimbursebal"));
			emp.setLastRenewal(row.getLocalDate("lastrenewal"));
	
			employees.put(emp.getId(), emp);
		});
		
//		return users;
		return new ArrayList<Employee>(employees.values());
	}
	
	public Department searchDept(Team name) {
		if(departments == null) {
			departments = new ArrayList<>();
			departments.add(new Department(Team.ALL));
			departments.add(new Department(Team.BOARD_OF_DIRECTORS));
			departments.add(new Department(Team.ACCOUNTING));
			departments.add(new Department(Team.ENGINEERING));
			departments.add(new Department(Team.IT));
			departments.add(new Department(Team.BENEFITS));
			departments.add(new Department(Team.NONE));
		}
		
		for(Department dept : departments) {
			if(dept.getName() == name) {
				return dept;
			}
		}
		return null;
	}
	
	public void configureDept(Employee emp, Team name) {
		emp.setDept(searchDept(name));
		// add employee as department head if it's their title
		if(name == Team.ALL && emp.getRole() == Role.FOUNDER) {
			emp.getDept().setDeptHead(emp.getUsername());
			emp.getDept().getMembers().put(emp.getId(), emp.getUsername());
			System.out.println("New member (" + emp.getName() + ") for Department " + name.toString() + " added as " + emp.getRole().toString());
			return;
		}
		
		if(emp.getRole() == Role.DEPARTMENT_HEAD) {
			emp.getDept().setDeptHead(emp.getUsername());
			emp.getDept().getMembers().put(emp.getId(), emp.getUsername());
			System.out.println("New member (" + emp.getName() + ") for Department " + name.toString() + " added as " + emp.getRole().toString());
		}
		else {
			emp.getDept().getMembers().put(emp.getId(), emp.getUsername());
			System.out.println("New member (" + emp.getName() + ") for Department " + name.toString() + " added as " + emp.getRole().toString());
		}
	}
	
	public void configureApprovalChain(Employee emp) {
		// Set approval chain based on employee type
		Employee next_approver = null;
		Department benefits = searchDept(Team.BENEFITS);
		List<String> members = new ArrayList<String>(benefits.getMembers().values());
		for(String username : members) {
			if(!username.equalsIgnoreCase(emp.getUsername())) {
				next_approver = search(username);
				if(next_approver.getRole() == Role.COORDINATOR) {
					next_approver = search(username); 
					break;
				}
			}
		}

		// If FOUNDER - requires approval from benCo only
		emp.getApprovalChain().add(next_approver.getUsername());
		
		if(emp.getRole() == Role.FOUNDER) {
			System.out.println("Approval chain configured for " + emp.getUsername() + " as follows: ");
			showApprovalChain(emp);
			return;
		}
		
		// If CEO or Dept Head - requires approval from FOUNDER and BenCo
		// If Manager - requires approval from Dept head & BenCo
		if(emp.getRole() != Role.COORDINATOR) {
			next_approver = search(emp.getManager());
			emp.getApprovalChain().add(next_approver.getUsername());
			System.out.println("Approval chain configured for " + emp.getUsername() + " as follows: ");
			showApprovalChain(emp);
			return;
		}		
		
		// If Coordinator - requires approval from Manager / Supervisor, Department head and BenCo
		Employee mgr = search(emp.getDept().getDeptHead());
		next_approver = mgr;
		emp.getApprovalChain().add(next_approver.getUsername());
		next_approver = search(emp.getManager());
		emp.getApprovalChain().add(next_approver.getUsername());
		System.out.println("Approval chain configured for " + emp.getUsername() + " as follows: ");
		showApprovalChain(emp);
		update(emp);
		return;

	}
	
	public void showApprovalChain(Employee emp) {
		if(emp.getApprovalChain().size() <= 0) {
			System.out.println("Approval chain not configured");
			return;
		}
		
		System.out.println("Number of appprovals in chain: " + emp.getApprovalChain().size());
		for(String s : emp.getApprovalChain()) 
			System.out.println("Approval required from " + s);
		
		
	}
	
	public List<UUID> getRequestHistory(String username) {
		
		String query = "Select history from employee where username = ?";
		SimpleStatement s = new SimpleStatementBuilder(query).build();
		BoundStatement bound = session.prepare(s).bind(username);
		// ResultSet is the values returned by my query.
		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		if(row == null) {
			// if there is no return value
			return null;
		}
		List<UUID> req_history = row.getList("history", UUID.class);
		return req_history;
	}
		
	public List<UUID> qToList(Queue<Request> q) {
		// extract ID's then put in returned array
		List<UUID> result = new ArrayList<>();

		if(q == null) {
			return result;
		}
		
		List<Request> temp = new ArrayList<Request>(q);
		for(Request req : temp) {
			result.add(req.getReqID());
		}
		
		return result;
	}

	public Employee search(String username) {
		System.out.println("Searching for employee with username: " + username);
//		
		System.out.println("No employees found in local records during search...\nRepopulating data structures from DB");
//		getEmployees();
		if(employees != null && employees.size() > 0) {
			System.out.println("Searching employee DB of size: " + employees.size() + "\nfor " + username);
			System.out.println(EmployeeDAOImpl.employees.toString());
			for(Employee e : new ArrayList<Employee>(employees.values())) {
				if(e.getUsername().equalsIgnoreCase(username)) {
					System.out.println("Found " + username + " in employee list");
					return e;
				}
			}
		}
				
		Employee emp = get(username);
		if(emp != null) {
			System.out.println("Found " + username + " in database");
			return emp;
		}
		
		System.out.println("No employee found with username: " + username);
		return null;
	}

}
