package com.revature;

import java.time.LocalDate;

import com.revature.data.EmployeeDAOImpl;
import com.revature.data.RequestDAOImpl;
import com.revature.model.Coverage;
import com.revature.model.Employee;
import com.revature.model.Request;
import com.revature.model.Role;
import com.revature.model.Team;
import com.revature.services.EmployeeService;
import com.revature.services.RequestService;
import com.revature.services.ThreadService;
import com.revature.util.CassandraUtil;


public class DataBaseInitializer {
	public EmployeeDAOImpl empDao;
	public RequestDAOImpl reqDao;
	
	
	public DataBaseInitializer() {
		empDao = new EmployeeDAOImpl();
		reqDao = new RequestDAOImpl();
	}
	
	public DataBaseInitializer(EmployeeDAOImpl empDao, RequestDAOImpl reqDao) {
		super();
		this.empDao = empDao;
		this.reqDao = reqDao;
	}
	
	public void dropTables() {
		StringBuilder sb = new StringBuilder("DROP TABLE IF EXISTS Employee;");
		CassandraUtil.getInstance().getSession().execute(sb.toString());

		try {
			Thread.sleep(4000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}	
		
		sb = new StringBuilder("DROP TABLE IF EXISTS Request;");
		CassandraUtil.getInstance().getSession().execute(sb.toString());

		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}

	public void createTables() {
		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS Employee (")
			.append("id int, username text, name text, email text, message text, birthday date, department text, role text, manager text, approvalchain List<text>, ")
			.append("pendingreview List<uuid>, history List<uuid>, reimburserecvd double, reimbursebal double, lastrenewal date, primary key (username));");
		CassandraUtil.getInstance().getSession().execute(sb.toString());
		
		System.out.println("Created Employee Table");

		try {
			Thread.sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		

		sb = new StringBuilder("CREATE TABLE IF NOT EXISTS Request (")
			.append("req_id uuid, description text, type text, requestor text, next_approver text, cost double, reimburse_amount double, docs List<text>, requestees List<text>, passing_grade text, ")
			.append("event_date date, submission_date date, status text, priority text, comment text, commHistory text, primary key (req_id, requestor));");
		CassandraUtil.getInstance().getSession().execute(sb.toString());

		System.out.println("Created Request Table");

		try {
			Thread.sleep(40000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
	}

	
	public void populateEmployeeTable() {
		
		Employee emp = new Employee("xspark", "Steve G", "spark@sparkPro.com", LocalDate.of(1960, 10, 1), Role.FOUNDER);
		empDao.add(emp, Team.ALL);
//		ed.updateEmployee(emp);
		emp = new Employee("mars", "MF", "mars@sparkPro.com", LocalDate.of(1970, 10, 1), Role.DEPARTMENT_HEAD);
		empDao.add(emp, Team.ACCOUNTING);
		emp.setManager("xspark");
		Employee emp1 = new Employee("dave", "Dave G", "dave@sparkPro.com", LocalDate.of(1951, 10, 1), Role.CEO);
		emp1.setManager("xspark");
		empDao.add(emp1, Team.ALL);
		Employee emp2 = new Employee("manie", "Emma G", "manie@sparkPro.com", LocalDate.of(1942, 10, 1), Role.CEO);
		emp2.setManager("xspark");
		empDao.add(emp2, Team.ALL);
		Employee emp3 = new Employee("sharry", "Sharon G", "sharry@sparkPro.com", LocalDate.of(1972, 10, 1), Role.DEPARTMENT_HEAD);
		emp3.setManager("xspark");
		empDao.add(emp3, Team.BENEFITS);
		Employee emp4 = new Employee("annie", "Joanne G", "jojo@sparkPro.com", LocalDate.of(1989, 10, 1), Role.DEPARTMENT_HEAD);
		emp4.setManager("xspark");
		empDao.add(emp4, Team.ENGINEERING);
		Employee emp5 = new Employee("yen", "Yen D", "yen@sparkPro.com", LocalDate.of(1950, 10, 1), Role.MANAGER);
		emp5.setManager(emp.getUsername());
		empDao.add(emp5, Team.ACCOUNTING);
		Employee emp6 = new Employee("meh", "Naomi H", "naomi@sparkPro.com", LocalDate.of(1960, 10, 1), Role.COORDINATOR);
		emp6.setManager(emp5.getUsername());
		empDao.add(emp6, Team.ACCOUNTING);
		Employee emp7 = new Employee("dm", "Diane M", "dm@sparkPro.com", LocalDate.of(1955, 10, 1), Role.MANAGER);
		emp7.setManager(emp3.getUsername());
		empDao.add(emp7, Team.BENEFITS);
		Employee emp8 = new Employee("jessy", "Jessy Q", "dm@sparkPro.com", LocalDate.of(1942, 10, 1), Role.COORDINATOR);
		emp8.setManager(emp7.getUsername());
		empDao.add(emp8, Team.BENEFITS);
		Employee emp9 = new Employee("britt", "Brittney S", "britt@sparkPro.com", LocalDate.of(1977, 10, 1), Role.COORDINATOR);
		emp9.setManager(emp7.getUsername());
		empDao.add(emp9, Team.BENEFITS);
		
		// configure approval chain
		empDao.configureApprovalChain(emp);
		empDao.configureApprovalChain(emp1);
		empDao.configureApprovalChain(emp2);
		empDao.configureApprovalChain(emp3);
		empDao.configureApprovalChain(emp4);
		empDao.configureApprovalChain(emp5);
		empDao.configureApprovalChain(emp6);
		empDao.configureApprovalChain(emp7);
		empDao.configureApprovalChain(emp8);
		empDao.configureApprovalChain(emp9);


	}
	
	public void simulateRequests() {
		
		
		Request req = new Request("Reimbursement for Summer class: Organic Chem 201",
				Coverage.UNIVERSITY_COURSES, "meh", 2000.0, LocalDate.of(2021, 7, 27));
		reqDao.add(req, empDao.search(req.getRequestor()));
		Request req1 = new Request("Reimbursement for Cert Course: CCNA",
				Coverage.CERT_PREP, "annie", 500.0, LocalDate.of(2021, 8, 27));
		reqDao.add(req1, empDao.search(req1.getRequestor()));
		Request req2 = new Request("New Era for Accounting",
				Coverage.SEMINARS, "meh", 200.0, LocalDate.of(2021, 9, 27));
		reqDao.add(req2, empDao.search(req2.getRequestor()));
		
		reqDao.getAllRequests();
		RequestDAOImpl.sortRequests();
	}

	public void initiateThreads() {
		ThreadService tserv = new ThreadService();
		tserv.startService(null);
	}
}
