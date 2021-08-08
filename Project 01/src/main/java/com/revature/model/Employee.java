package com.revature.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;


public class Employee implements Serializable {
	private static final long serialVersionUID = 7426075925303078710L;
	
	private static int count;
	private static LocalDate today = LocalDate.now();
	
	private String username;
	private Integer id;						// auto
	private String name;
	private String email;
	private String message;
	private LocalDate birthday;
	private Department dept;
	private Role role;
	private String manager;
	private Queue<Request> pendingReview;
	private List<Request> history;			// auto	
	private Double reimburseRecvd;			// auto
	private Double reimburseBalance;		// auto
	private LocalDate lastRenewal;
	
	public Employee() {
		super();
		this.id = ++count;
		this.lastRenewal = LocalDate.of(LocalDate.now().getYear(), 1, 1);
		this.reimburseBalance = 1000.0;
		this.reimburseRecvd = 0.0;
		this.message = "";
	}
	
	public Employee(String user, String name, String email, LocalDate birthday, Role role) {
		this.id = ++count;
		this.username = user;
		this.name = name;
		this.email = email;
		this.birthday = birthday;
		this.role = role;
		this.lastRenewal =  LocalDate.of(LocalDate.now().getYear(), 1, 1);;
		this.reimburseBalance = 1000.0;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}

	public Department getDept() {
		return dept;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public void setDept(Department dept) {
		this.dept = dept;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public String getManager() {
		return manager;
	}

	public void setManager(String manager) {
		this.manager = manager;
	}

	public Queue<Request> getPendingReview() {
		return pendingReview;
	}

	public void setPendingReview(Queue<Request> pendingReview) {
		this.pendingReview = pendingReview;
	}

	public List<Request> getHistory() {
		if(history == null) {
			history = new ArrayList<Request>();
		}
		return history;
	}

	public void setHistory(List<Request> history) {
		this.history = history;
	}

	public Double getReimburseRecvd() {
		return reimburseRecvd;
	}

	public void setReimburseRecvd(Double reimburseRecvd) {
		this.reimburseRecvd = reimburseRecvd;
	}

	public Double getReimburseBalance() {
//		int last_renewal = this.lastRenewal.getYear();
//		this.reimburseBalance = ++last_renewal == today.getYear() ? 1000.0 : reimburseBalance;		
		return reimburseBalance;
	}

	public void setReimburseBalance(Double reimburseBalance) {
		this.reimburseBalance = reimburseBalance;
	}

	public LocalDate getLastRenewal() {
		return lastRenewal;
	}

	public void setLastRenewal(LocalDate renewal) {
		this.lastRenewal = renewal;
	}

	public static int getCount() {
		return count;
	}

	public static void setCount(int count) {
		Employee.count = count;
	}

	public static LocalDate getToday() {
		return today;
	}

	public static void setToday(LocalDate today) {
		Employee.today = today;
	}

}
