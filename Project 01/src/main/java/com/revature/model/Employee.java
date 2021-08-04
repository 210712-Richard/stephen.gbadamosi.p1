package com.revature.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

enum Role { CEO, DEPARTMENT_HEAD, MANAGER, SUPERVISOR, COORDINATOR }

public class Employee implements Serializable {
	private static final long serialVersionUID = 7426075925303078710L;
	
	private String username;
	private Integer id;
	private String name;
	private String email;
	private Department dept;
	private Role job;
	private LocalDate birthday;
	private Request req;
	private List<Request> history;
	private Double reimburseBalance;
	private LocalDate renewalDate;
	
	public Employee() {
		super();
		this.renewalDate = LocalDate.now();
		this.reimburseBalance = 1000.0;
	}
	
	public Employee(String user, String name, String email, Department dept, Role job) {
		// set UUID
	//	this.uid = ;
		this.name = name;
		this.email = email;
		this.dept = dept;
		this.job = job;
		this.renewalDate = LocalDate.now();
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

	public Department getDept() {
		return dept;
	}

	public void setDept(Department dept) {
		this.dept = dept;
	}

	public Role getJob() {
		return job;
	}

	public void setJob(Role job) {
		this.job = job;
	}

	public LocalDate getBirthday() {
		return birthday;
	}

	public void setBirthday(LocalDate birthday) {
		this.birthday = birthday;
	}

	public Request getReq() {
		return req;
	}

	public void setReq(Request req) {
		this.req = req;
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

	public Double getReimburseBalance() {
		return reimburseBalance;
	}

	public void setReimburseBalance(Double reimburseBalance) {
		this.reimburseBalance = reimburseBalance;
	}

	public LocalDate getRenewalDate() {
		return renewalDate;
	}

	public void setRenewalDate(LocalDate renewalDate) {
		this.renewalDate = renewalDate;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}
}
