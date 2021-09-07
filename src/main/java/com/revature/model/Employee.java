package com.revature.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Stack;


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
	private Stack<String> approvalChain;
	private Queue<Request> pendingReview;
	private List<Request> history;			// auto	
	private Double reimburseRecvd;			// auto
	private Double reimburseBalance;		// auto
	private LocalDate lastRenewal;
	
	
	public Employee() {
		super();
		this.lastRenewal = LocalDate.of(LocalDate.now().getYear(), 1, 1);
		this.reimburseBalance = 1000.0;
		this.reimburseRecvd = 0.0;
		this.message = "";
	}
	
	public Employee(String username) {
//		super();
		this.username = username;
		this.lastRenewal = LocalDate.of(LocalDate.now().getYear(), 1, 1);
		this.reimburseBalance = 1000.0;
		this.reimburseRecvd = 0.0;
		this.message = "";
		this.approvalChain = new Stack<String>();
		this.pendingReview = new LinkedList<Request>();
	}
	
	public Employee(String user, String name, String email, LocalDate birthday, Role role) {
		this.id = ++count;
		this.username = user;
		this.name = name;
		this.email = email;
		this.birthday = birthday;
		this.role = role;
		this.history = new ArrayList<>();
		this.lastRenewal =  LocalDate.of(LocalDate.now().getYear(), 1, 1);;
		this.reimburseBalance = 1000.0;
		this.approvalChain = new Stack<String>();
		this.pendingReview = new LinkedList<Request>();
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

	public Stack<String> getApprovalChain() {
		if(approvalChain == null) 
			approvalChain = new Stack<>();
		
		return approvalChain;
	}

	public void setApprovalChain(Stack<String> approvalChain) {
		this.approvalChain = approvalChain;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((birthday == null) ? 0 : birthday.hashCode());
		result = prime * result + ((dept == null) ? 0 : dept.hashCode());
		result = prime * result + ((email == null) ? 0 : email.hashCode());
		result = prime * result + ((history == null) ? 0 : history.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((lastRenewal == null) ? 0 : lastRenewal.hashCode());
		result = prime * result + ((manager == null) ? 0 : manager.hashCode());
		result = prime * result + ((message == null) ? 0 : message.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((pendingReview == null) ? 0 : pendingReview.hashCode());
		result = prime * result + ((reimburseBalance == null) ? 0 : reimburseBalance.hashCode());
		result = prime * result + ((reimburseRecvd == null) ? 0 : reimburseRecvd.hashCode());
		result = prime * result + ((role == null) ? 0 : role.hashCode());
		result = prime * result + ((username == null) ? 0 : username.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Employee other = (Employee) obj;
		if (birthday == null) {
			if (other.birthday != null)
				return false;
		} else if (!birthday.equals(other.birthday))
			return false;
		if (dept == null) {
			if (other.dept != null)
				return false;
		} else if (!dept.equals(other.dept))
			return false;
		if (email == null) {
			if (other.email != null)
				return false;
		} else if (!email.equals(other.email))
			return false;
		if (history == null) {
			if (other.history != null)
				return false;
		} else if (!history.equals(other.history))
			return false;
		if (lastRenewal == null) {
			if (other.lastRenewal != null)
				return false;
		} else if (!lastRenewal.equals(other.lastRenewal))
			return false;
		if (manager == null) {
			if (other.manager != null)
				return false;
		} else if (!manager.equals(other.manager))
			return false;
		if (message == null) {
			if (other.message != null)
				return false;
		} else if (!message.equals(other.message))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (pendingReview == null) {
			if (other.pendingReview != null)
				return false;
		} else if (!pendingReview.equals(other.pendingReview))
			return false;
		if (reimburseBalance == null) {
			if (other.reimburseBalance != null)
				return false;
		} else if (!reimburseBalance.equals(other.reimburseBalance))
			return false;
		if (reimburseRecvd == null) {
			if (other.reimburseRecvd != null)
				return false;
		} else if (!reimburseRecvd.equals(other.reimburseRecvd))
			return false;
		if (role != other.role)
			return false;
		if (username == null) {
			if (other.username != null)
				return false;
		} else if (!username.equals(other.username))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "Employee [username=" + username + ", id=" + id + ", name=" + name + ", email=" + email + ", message="
				+ message + ", birthday=" + birthday + ", dept=" + dept + ", role=" + role + ", manager=" + manager
				+ ", pendingReview=" + pendingReview + ", history=" + history + ", reimburseRecvd=" + reimburseRecvd
				+ ", reimburseBalance=" + reimburseBalance + ", lastRenewal=" + lastRenewal + "]";
	}

}
