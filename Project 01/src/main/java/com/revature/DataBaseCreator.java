package com.revature;

import java.time.LocalDate;

import com.revature.util.CassandraUtil;

public class DataBaseCreator {
	
	public static void dropTables() {
		StringBuilder sb = new StringBuilder("DROP TABLE IF EXISTS Employee;");
		CassandraUtil.getInstance().getSession().execute(sb.toString());

		
		sb = new StringBuilder("DROP TABLE IF EXISTS Request;");
		CassandraUtil.getInstance().getSession().execute(sb.toString());

		try {
			Thread.sleep(7000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	public static void createTables() {
		StringBuilder sb = new StringBuilder("CREATE TABLE IF NOT EXISTS Employee (")
			.append("id uuid, username text, name text, role text, department text, email text, birthday date, req uuid, ")
			.append("history List<varint>, renewal date, reimburseBal float, primary key (department, role));");
		CassandraUtil.getInstance().getSession().execute(sb.toString());
		

		sb = new StringBuilder("CREATE TABLE IF NOT EXISTS Request (")
			.append("id uuid, username text, status text, role text, department text, email text, birthday date, req uuid, ")
			.append("history List<varint>, renewal date, reimburseBal float, primary key (department, role));");
		CassandraUtil.getInstance().getSession().execute(sb.toString());
		
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
				
	}

	
//	public static void populateEmployeeTable() {
//		User u = new User("Richard","richard.orr@revature.com", LocalDate.of(1960, 8, 30), 2000l);
//		u.setType(UserType.GAME_MASTER);
//		ud.addUser(u);
//		ud.addUser(new User("Michael", "michael@michael.michael", LocalDate.of(1700, 5, 6), 1000l));
//		ud.addUser(new User("Jaclyn", "jaclyn@jaclyn.jaclyn", LocalDate.of(1660, 4, 2), 1000l));
//		ud.addUser(new User("Joshua", "one@josh.alltime", LocalDate.of(1984, 1, 25), 1000l));
//		ud.addUser(new User("Stephen", "stephen@steven.steve", LocalDate.of(1880, 7, 23), 1000l));
//	}

}
