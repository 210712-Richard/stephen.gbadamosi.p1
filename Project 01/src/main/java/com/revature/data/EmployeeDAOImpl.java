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
import com.revature.util.CassandraUtil;

public class EmployeeDAOImpl implements EmployeeDAO {
	private CqlSession session = CassandraUtil.getInstance().getSession();
	private static HashMap<Integer, Employee> employees;
	private static List<Department> departments;


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
	@Override
	public void addEmployee(Employee emp, Team name) {
		emp.setDept(searchDept(name));
		String manager = emp.getManager() == null ? "None" : emp.getManager();
		emp.setManager(manager);
		String query = "Insert into employee (id, username, name, email, message, birthday, department, role, manager, "
				+ "pendingReview, history, reimburseRecvd, reimburseBal, lastRenewal) values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(emp.getId(), emp.getUsername(), emp.getName(), emp.getEmail(), emp.getMessage(), emp.getBirthday(),emp.getDept().toString(), Role.toString(emp.getRole()),
						 emp.getManager(), qToList(emp.getPendingReview()), emp.getHistory(), emp.getReimburseRecvd(), emp.getReimburseBalance(), emp.getLastRenewal());
		session.execute(bound);
		
		employees.put(emp.getId(), emp); 	// review: may not be necessary
		System.out.println("Adding employee to Team: " + name);
		emp.getDept().getMembers().add(emp);	// review may not be necessary
		
		System.out.println(emp.getName() + " has been successfully added to company database");
	}

	@Override
	public void updateEmployee(Employee emp) {
		String query = "Update employee set name = ?, email = ?, message = ?, birthday = ?, department = ?, role = ?, manager = ?, " +
		"pendingReview = ?, history = ?, reimburseRecvd = ?, reimburseBal = ?, lastRenewal = ? where username = ?;";
//		List<UUID> history = emp.getHistory()
//				.stream()
//				.filter(req -> req!=null)
//				.map(req -> req.getId())
//				.collect(Collectors.toList());
		SimpleStatement s = new SimpleStatementBuilder(query).setConsistencyLevel(DefaultConsistencyLevel.LOCAL_QUORUM).build();
		BoundStatement bound = session.prepare(s)
				.bind(emp.getName(), emp.getEmail(), emp.getMessage(), emp.getBirthday(), emp.getDept().toString(), Role.toString(emp.getRole()), emp.getManager(),
						qToList(emp.getPendingReview()), emp.getHistory(), emp.getReimburseRecvd(), emp.getReimburseBalance(), emp.getLastRenewal(), emp.getUsername());
		session.execute(bound);
		String apostrophe = emp.getUsername().substring(emp.getUsername().length()-1).equals("s") ? "'" : "'s";
		System.out.println(emp.getUsername() + apostrophe + " account details updated successfully");
	}

	@Override
	public Employee getEmployee(String username) {
		String query = "Select id, username, name, email, message, birthday, department, role, manager, "
		+ "pendingReview, history, reimburseRecvd, reimburseBal, lastRenewal from employee where username=?";
		SimpleStatement s = new SimpleStatementBuilder(query).build();
		BoundStatement bound = session.prepare(s).bind(username);

		ResultSet rs = session.execute(bound);
		Row row = rs.one();
		
		if(row == null) {
			System.out.println("No employee registered in database with username: " + username);
			// if there are no return values
			return null;
		}
		
		Employee emp = new Employee();
		emp.setUsername(row.getString("username"));
		emp.setEmail(row.getString("email"));
		emp.setMessage(row.getString("message"));
		emp.setBirthday(row.getLocalDate("birthday"));
		emp.setDept(searchDept(Team.valueOf(row.getString("department"))));
		emp.setRole(Role.valueOf(row.getString("role")));
		emp.setManager(row.getString("manager"));
		emp.setReimburseRecvd(row.getDouble("reimburseRecvd"));
		emp.setReimburseBalance(row.getDouble("reimburseBal"));
		emp.setLastRenewal(row.getLocalDate("lastRenewal"));
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
		String query = "Select id, username, name, email, message, birthday, department, role, manager, "
		+ "pendingReview, history, reimburseRecvd, reimburseBal, lastRenewal from employee";
		SimpleStatement s = new SimpleStatementBuilder(query).build();

		ResultSet rs = session.execute(s);
		if(rs.one() == null) {
			System.out.println("No employees currrently registered in database");
			// if there are no return values
			return null;
		}
		
		rs.forEach(row -> {	
			Employee emp = new Employee();
			emp.setUsername(row.getString("username"));
			emp.setEmail(row.getString("email"));
			emp.setMessage(row.getString("message"));
			emp.setBirthday(row.getLocalDate("birthday"));
			emp.setDept(searchDept(Team.valueOf(row.getString("department"))));
			emp.setRole(Role.valueOf(row.getString("role")));
			emp.setManager(row.getString("manager"));
			emp.setReimburseRecvd(row.getDouble("reimburseRecvd"));
			emp.setReimburseBalance(row.getDouble("reimburseBal"));
			emp.setLastRenewal(row.getLocalDate("lastRenewal"));
	
			employees.put(emp.getId(), emp);
		});
		
//		return users;
		return new ArrayList<Employee>(employees.values());
	}
	
	public Department searchDept(Team name) {

		for(Department dept : departments) {
			if(dept.getName() == name) {
				return dept;
			}
		}
		return null;
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
	
	public static Employee searchEmployees(String username) {
		Employee emp = null;
		List<Employee> emps = new ArrayList<Employee>(employees.values());
		if(employees != null) {
			for(Employee e : emps) {
				if(e.getUsername().equalsIgnoreCase(username)) {
					System.out.println("Found " + username + " in employee list");
					emp = e;
				}
			}
		}
		
		return emp;
	}


}
