package com.revature;

import java.time.LocalDate;

import com.revature.data.EmployeeDAOImpl;
import com.revature.data.RequestDAOImpl;
import com.revature.model.Coverage;
import com.revature.model.Employee;
import com.revature.model.Request;
import com.revature.model.Role;
import com.revature.model.Team;
import com.revature.util.CassandraUtil;


public class DataBaseInitializer {
	private static EmployeeDAOImpl ed = new EmployeeDAOImpl();
	private static RequestDAOImpl rd = new RequestDAOImpl();
	
	
	public static void dropTables() {
		StringBuilder sb = new StringBuilder("DROP TABLE IF EXISTS Employee;");
		CassandraUtil.getInstance().getSession().execute(sb.toString());

		try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		sb = new StringBuilder("DROP TABLE IF EXISTS Request;");
		CassandraUtil.getInstance().getSession().execute(sb.toString());

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

	public static void createTables() {
		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS Employee (")
			.append("id int, username text, name text, email text, message text, birthday date, department text, role text, manager text, ")
			.append("pendingReview List<uuid>, history List<uuid>, reimburseRecvd double, reimburseBal double, lastRenewal date, primary key (username));");
		CassandraUtil.getInstance().getSession().execute(sb.toString());

		try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		

		sb = new StringBuilder("CREATE TABLE IF NOT EXISTS Request (")
			.append("req_id uuid, description text, coverage text, requestor text, cost double, reimburse_amount double, docsURL text, passing_grade text, ")
			.append("event_date date, submission_date date, status text, priority text, comment text, commHistory text, primary key (req_id, requestor));");
		CassandraUtil.getInstance().getSession().execute(sb.toString());
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
	}

	
	public static void populateEmployeeTable() {
		
		
		Employee emp = new Employee("xspark", "Steve G", "spark@sparkPro.com", LocalDate.of(1960, 10, 1), Role.FOUNDER);
		ed.addEmployee(emp, Team.ALL);
//		ed.updateEmployee(emp);
		emp = new Employee("mars", "MF", "mars@sparkPro.com", LocalDate.of(1970, 10, 1), Role.DEPARTMENT_HEAD);
		ed.addEmployee(emp, Team.ACCOUNTING);
		Employee emp1 = new Employee("dave", "Dave G", "dave@sparkPro.com", LocalDate.of(1951, 10, 1), Role.CEO);
		ed.addEmployee(emp1, Team.ALL);
		Employee emp2 = new Employee("manie", "Emma G", "manie@sparkPro.com", LocalDate.of(1942, 10, 1), Role.CEO);
		ed.addEmployee(emp2, Team.ALL);
		Employee emp3 = new Employee("sharry", "Sharon G", "sharry@sparkPro.com", LocalDate.of(1972, 10, 1), Role.DEPARTMENT_HEAD);
		ed.addEmployee(emp3, Team.BENEFITS);
		Employee emp4 = new Employee("annie", "Joanne G", "jojo@sparkPro.com", LocalDate.of(1989, 10, 1), Role.DEPARTMENT_HEAD);
		ed.addEmployee(emp4, Team.ENGINEERING);
		Employee emp5 = new Employee("yen", "Yen D", "yen@sparkPro.com", LocalDate.of(1950, 10, 1), Role.MANAGER);
		emp5.setManager(emp.getUsername());
		ed.addEmployee(emp5, Team.ACCOUNTING);
		Employee emp6 = new Employee("meh", "Naomi H", "naomi@sparkPro.com", LocalDate.of(1960, 10, 1), Role.COORDINATOR);
		emp6.setManager(emp5.getUsername());
		ed.addEmployee(emp6, Team.ACCOUNTING);
	}
	
	public static void simulateNewRequests() {
		Request req = new Request("Reimbursement for Summer class: Organic Chem 201",
				Coverage.UNIVERSITY_COURSES, "meh", 2000.0, LocalDate.of(2021, 7, 27));
		rd.addRequest(req, ed.searchEmployees(req.getRequestor()));
		Request req1 = new Request("Reimbursement for Cert Course: CCNA",
				Coverage.CERT_PREP, "xspark", 500.0, LocalDate.of(2021, 7, 27));
		rd.addRequest(req1, ed.searchEmployees(req1.getRequestor()));
		Request req2 = new Request("New Era for Accounting",
				Coverage.SEMINARS, "meh", 200.0, LocalDate.of(2021, 5, 27));
		rd.addRequest(req2, ed.searchEmployees(req2.getRequestor()));
	}

}
