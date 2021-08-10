package com.revature.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Queue;
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
import com.revature.services.EmployeeService;
import com.revature.util.CassandraUtil;

public class EmployeeDAOImpl implements EmployeeDAO {
	private CqlSession session = CassandraUtil.getInstance().getSession();
	private static RequestDAOImpl rd = new RequestDAOImpl();
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
	
	{
		// populate local employees database
		getEmployees();
	}
	
	@Override
	public void add(Employee emp, Team name) {
		if(employees == null) {
			employees = new HashMap<>();
		}
	// Set new employee department before add
		configureDept(emp, name);
		String manager = emp.getManager() == null ? "None" : emp.getManager();
		emp.setManager(manager);
		
		String query;
		if((searchEmployees(emp.getUsername())) != null) {
			System.out.println("Employee already registered in DB - updating account details instead");
			
			query = "Update employee Set name = ?, email = ?, message = ?, department = ?, role = ?, manager = ?, " +
			"history = ?, reimburserecvd = ?, reimbursebal = ? where username = ?;";

			SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
			BoundStatement bound = session.prepare(s)
					.bind(emp.getName(), emp.getEmail(), emp.getMessage(), emp.getDept().toString(), Role.toString(emp.getRole()), emp.getManager(),
							RequestDAOImpl.transformRequest(emp.getHistory()), emp.getReimburseRecvd(), emp.getReimburseBalance(), emp.getUsername());
			session.execute(bound);
			String apostrophe = emp.getUsername().substring(emp.getUsername().length()-1).equals("s") ? "'" : "'s";
			System.out.println(emp.getUsername() + apostrophe + " account details updated successfully");
			return;
		}
		
		query = "Insert into employee (id, username, name, email, message, birthday, department, role, manager, "
				+ "pendingReview, history, reimburserecvd, reimbursebal, lastRenewal) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(emp.getId(), emp.getUsername(), emp.getName(), emp.getEmail(), emp.getMessage(), emp.getBirthday(),emp.getDept().toString(), Role.toString(emp.getRole()),
						 emp.getManager(), qToList(emp.getPendingReview()), RequestDAOImpl.transformRequest(emp.getHistory()), emp.getReimburseRecvd(), emp.getReimburseBalance(), emp.getLastRenewal());
		session.execute(bound);
		
		employees.put(emp.getId(), emp); 	// review: may not be necessary
		System.out.println("Adding employee to Team: " + name);
		emp.getDept().getMembers().add(emp);	// review may not be necessary
		
		System.out.println(emp.getName() + " has been successfully added to company database");
	}

	@Override
	public void update(Employee emp) {
		String query = "Update employee Set name = ?, email = ?, message = ?, birthday = ?, department = ?, role = ?, manager = ?, " +
		"pendingreview = ?, history = ?, reimburserecvd = ?, reimbursebal = ?, lastrenewal = ? where username = ?;";
//		List<UUID> history = emp.getHistory()
//				.stream()
//				.filter(req -> req!=null)
//				.map(req -> req.getId())
//				.collect(Collectors.toList());
		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(emp.getName(), emp.getEmail(), emp.getMessage(), emp.getBirthday(), emp.getDept().toString(), Role.toString(emp.getRole()), emp.getManager(),
						qToList(emp.getPendingReview()), RequestDAOImpl.transformRequest(emp.getHistory()), emp.getReimburseRecvd(), emp.getReimburseBalance(), emp.getLastRenewal(), emp.getUsername());
		session.execute(bound);
		String apostrophe = emp.getUsername().substring(emp.getUsername().length()-1).equals("s") ? "'" : "'s";
		System.out.println(emp.getUsername() + apostrophe + " account details updated successfully");
	}

	@Override
	public Employee get(String username) {
		String query = "Select id, username, name, email, message, birthday, department, role, manager, "
		+ "pendingReview, history, reimburserecvd, reimbursebal, lastrenewal from employee where username = ?";
		SimpleStatement s = new SimpleStatementBuilder(query).build();
		BoundStatement bound = session.prepare(s).bind(username);

		ResultSet rs = session.execute(bound);
		
		if(rs == null) {
			System.out.println("No employee registered in database with username: " + username);
			// if there are no return values
			return null;
		}
		Row row = rs.one();
		
		Employee emp = new Employee();
		emp.setUsername(row.getString("username"));
		emp.setEmail(row.getString("email"));
		emp.setMessage(row.getString("message"));
		emp.setBirthday(row.getLocalDate("birthday"));
		List<UUID> history = row.getList("history", UUID.class);
		if(history != null && history.size() > 0)
			emp.setHistory(rd.revertRequest(history));
		
		else {
			List<Request> new_list = new ArrayList<Request>();
			emp.setHistory(new_list);
		}
				emp.setDept(searchDept(Team.valueOf(row.getString("department"))));
		emp.setRole(Role.valueOf(row.getString("role")));
		emp.setManager(row.getString("manager"));
		emp.setReimburseRecvd(row.getDouble("reimburserecvd"));
		emp.setReimburseBalance(row.getDouble("reimbursebal"));
		emp.setLastRenewal(row.getLocalDate("lastrenewal"));
////	row = rs.one();
////	if(row != null)
////		throw new RuntimeException("More than one employee with same username");
////
		
		return emp;
	}

//	@Override
//	public Employee getEmployeeByID(Integer eid) {
//		String query = "Select username, type, email, currency, birthday, lastCheckIn from user where username=?";
//		SimpleStatement s = new SimpleStatementBuilder(query).build();
//		BoundStatement bound = session.prepare(s).bind(username);
//		// ResultSet is the values returned by my query.
//		ResultSet rs = session.execute(bound);
//		Row row = rs.one();
//		if(row == null) {
//			// if there is no return values
//			return null;
//		}
//		User u = new User();
//		u.setUsername(row.getString("username"));
//		u.setEmail(row.getString("email"));
//		u.setCurrency(row.getLong("currency"));
//		u.setType(UserType.valueOf(row.getString("type")));
//		u.setBirthday(row.getLocalDate("birthday"));
//		u.setLastCheckIn(row.getLocalDate("lastcheckin"));
////		row = rs.one();
////		if(row != null) {
////			throw new RuntimeException("More than one user with same username");
////		}
//		return u;
//		return null;
//	}

	@Override
	public List<Employee> getEmployees() {
		if(employees == null) {
			employees = new HashMap<>();
		}
		
		String query = "Select id, username, name, email, message, birthday, department, role, manager, "
		+ "pendingReview, history, reimburseRecvd, reimburseBal, lastRenewal from employee";
		SimpleStatement s = new SimpleStatementBuilder(query).build();

		ResultSet rs = session.execute(s);
		if(rs == null) {
			System.out.println("No employees currrently registered in database");
			// if there are no return values
			return null;
		}
		
		
		
		rs.forEach(row -> {	
			Employee emp = new Employee();
			emp.setId(row.getInt("id"));
			emp.setUsername(row.getString("username"));
			emp.setName(row.getString("name"));
			emp.setEmail(row.getString("email"));
			emp.setMessage(row.getString("message"));
			emp.setBirthday(row.getLocalDate("birthday"));
			List<UUID> history = row.getList("history", UUID.class);
			if(history != null && history.size() > 0)
				emp.setHistory(rd.revertRequest(history));
			
			else {
				List<Request> new_list = new ArrayList<Request>();
				emp.setHistory(new_list);
			}
			
			emp.setDept(searchDept(Team.valueOf(row.getString("department"))));
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
			emp.getDept().setDeptHead(emp);
			emp.getDept().getMembers().add(emp);
			System.out.println("New member (" + emp.getName() + ") for Department " + name.toString() + " added as " + emp.getRole().toString());
			return;
		}
		
		if(emp.getRole() == Role.DEPARTMENT_HEAD) {
			emp.getDept().setDeptHead(emp);
			emp.getDept().getMembers().add(emp);
			System.out.println("New member (" + emp.getName() + ") for Department " + name.toString() + " added as " + emp.getRole().toString());
		}
		else {
			emp.getDept().getMembers().add(emp);
			System.out.println("New member (" + emp.getName() + ") for Department " + name.toString() + " added as " + emp.getRole().toString());
		}
	}
	
	public void configureApprovalChain(Employee emp) {
		// Set approval chain based on employee type
		Employee next_approver = null;
		Department benefits = searchDept(Team.BENEFITS);
		for(Employee e : benefits.getMembers()) {
			if(!e.getUsername().equalsIgnoreCase(emp.getUsername())) {
				next_approver = e;
				if(next_approver.getRole() == Role.COORDINATOR) {
					next_approver = e; 
					break;
				}
			}
		}

		// If FOUNDER - requires approval from benCo only
		emp.getApprovalChain().add(next_approver);
		
		if(emp.getRole() == Role.FOUNDER) {
			System.out.println("Approval chain configured for " + emp.getUsername() + " as follows: ");
			showApprovalChain(emp);
			return;
		}
		
		// If CEO or Dept Head - requires approval from FOUNDER and BenCo
		// If Manager - requires approval from Dept head & BenCo
		if(emp.getRole() != Role.COORDINATOR) {
			next_approver = searchEmployees(emp.getManager());
			emp.getApprovalChain().add(next_approver);
			System.out.println("Approval chain configured for " + emp.getUsername() + " as follows: ");
			showApprovalChain(emp);
			return;
		}		
		
		// If Coordinator - requires approval from Manager / Supervisor, Department head and BenCo
		next_approver = searchDept(emp.getDept().getName()).getDeptHead();
		emp.getApprovalChain().add(next_approver);
		next_approver = searchEmployees(emp.getManager());
		emp.getApprovalChain().add(next_approver);
		System.out.println("Approval chain configured for " + emp.getUsername() + " as follows: ");
		showApprovalChain(emp);
		return;

//		// Add benefits coordinator or head to approval chain
//		Department temp = searchDept(Team.BENEFITS);
//		Employee next_approver = null;
//		if(temp.getMembers().size() == 1) {		// Only 1 member of team (department head) will approve
//			next_approver = temp.getMembers().get(0);
//			emp.getApprovalChain().add(next_approver);
//		}
//		else {	// Assign to first coordinator found in department
//			for(Employee e : temp.getMembers()) {
//				if(!e.getUsername().equalsIgnoreCase(emp.getUsername())) {
//					next_approver = e;
//					if(next_approver.getRole() == Role.COORDINATOR) {
//						emp.getApprovalChain().add(next_approver); 
//						break;
//					}
//				}
//				emp.getApprovalChain().add(next_approver);
//				
//			}
//		}
//		// Add Founder to approval chain if DH or CEO else add DH
//		if(emp.getRole() == Role.DEPARTMENT_HEAD || emp.getRole() == Role.CEO) {
//			next_approver = searchDept(Team.ALL).getDeptHead();
//			emp.getApprovalChain().add(next_approver);
//			emp.setManager(next_approver.getUsername());
//		}
//		
//		else {
//			next_approver = emp.getDept().getDeptHead();
//			emp.getApprovalChain().add(next_approver);
//			
//			// Add direct manager or supervisor
//			if(emp.getManager() == null) {
//				System.out.println("No manager assigned to " + emp.getUsername() + " to complete approval chain");
//				return;
//			}
//			
//			if(emp.getManager().equalsIgnoreCase(next_approver.getUsername())) {	// Employee is a manager so no further approval required
//				System.out.println("Approval chain configured for " + emp.getUsername() + " as follows: ");
//				showApprovalChain(emp);
//				return;
//			}
//			
//			else {
//				next_approver = EmployeeService.searchEmployees(emp.getManager());
//				emp.getApprovalChain().add(next_approver);
//				showApprovalChain(emp);
//				return;
//			}
//		}
	}
	
	public void showApprovalChain(Employee emp) {
		if(emp.getApprovalChain().size() <= 0) {
			System.out.println("Approval chain not configured");
			return;
		}
		
		System.out.println("Number of appprovals in chain: " + emp.getApprovalChain().size());
		for(Employee e : emp.getApprovalChain()) 
			System.out.println("Approval required from " + e.getName());
		
		
	}
	
	public List<UUID> getRequestHistory(String username) {
		
		String query = "Select history from employee where username = ?";
		SimpleStatement s = new SimpleStatementBuilder(query).build();
		BoundStatement bound = session.prepare(s).bind(username);
		// ResultSet is the values returned by my query.
		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		if(row == null) {
			// if there is no return values
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

	public Employee searchEmployees(String username) {
		System.out.println("Searching for employee with username: " + username);
		
		if(EmployeeDAOImpl.employees == null) {
			System.out.println("No employees found in local records during search...\nRepopulating data structures from DB");
			getEmployees();
		}
		System.out.println("Searching employee DB of size: " + EmployeeDAOImpl.employees.size() + "\nfor " + username);
		System.out.println(EmployeeDAOImpl.employees.toString());
		List<Employee> emps = new ArrayList<Employee>(EmployeeDAOImpl.employees.values());
		if(EmployeeDAOImpl.employees != null) {
			for(Employee e : emps) {
				if(e.getUsername().equalsIgnoreCase(username)) {
					System.out.println("Found " + username + " in employee list");
					return e;
				}
			}
		}
		
		System.out.println("No employee found with username: " + username);
		return null;
	}

}
