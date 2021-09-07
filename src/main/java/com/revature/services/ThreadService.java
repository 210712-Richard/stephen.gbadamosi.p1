package com.revature.services;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.revature.data.EmployeeDAOImpl;
import com.revature.data.RequestDAOImpl;
import com.revature.model.Employee;
import com.revature.model.Priority;
import com.revature.model.Request;
import com.revature.model.Role;
import com.revature.model.Status;
import com.revature.model.Team;

public class ThreadService {
	
	private int numThreads;
	private static RequestService reqServ;
	private static EmployeeDAOImpl empDao;
	private static RequestDAOImpl reqDao;
	private ExecutorService pool;
	
	public ThreadService() {
		numThreads = 4;
		pool = Executors.newFixedThreadPool(numThreads);
		empDao = new EmployeeDAOImpl();
		reqDao = new RequestDAOImpl();
		reqServ = new RequestService();
	}

	public ThreadService(int num) {
		numThreads = num;
		numThreads = numThreads != 0 ? numThreads : 4;
		
		pool = Executors.newFixedThreadPool(numThreads);
		empDao = new EmployeeDAOImpl();
		reqDao = new RequestDAOImpl();
		reqServ = new RequestService();		
	}
	
	public int getNumThreads() {
		return numThreads;
	}

	public void setNumThreads(int numThreads) {
		this.numThreads = numThreads;
	}

	public static EmployeeDAOImpl getEmpDao() {
		return empDao;
	}

	public static void setEmpDao(EmployeeDAOImpl empDao) {
		ThreadService.empDao = empDao;
	}

	public static RequestDAOImpl getReqDao() {
		return reqDao;
	}

	public static void setReqDao(RequestDAOImpl reqDao) {
		ThreadService.reqDao = reqDao;
	}

	public ExecutorService getPool() {
		return this.pool;
	}

	public void setPool(ExecutorService pool) {
		this.pool = pool;
	}
	
	@Override
	public String toString() {
		return "ThreadService [numThreads=" + numThreads + "]";
	}

	public void startService(Request req) {
		ThreadService executor = new ThreadService();
		if(req == null) {	// Start all threads
			executor.getPool().execute(() -> executor.runnableUrgentTask());
			executor.getPool().execute(() -> executor.runnableHighTask());
			executor.getPool().execute(() -> executor.runnableMediumTask());
			executor.getPool().execute(() -> executor.runnableLowTask());
			return;
		}
		
		switch(req.getSLA()) {
			case URGENT:
				executor.getPool().execute(() -> executor.runnableUrgentTask());
				break;
			case HIGH:
				executor.getPool().execute(() -> executor.runnableHighTask());
				break;
			case MEDIUM:
				executor.getPool().execute(() -> executor.runnableMediumTask());
				break;
			case LOW:
				executor.getPool().execute(() -> executor.runnableLowTask());
				break;
				
			default: // Run all tasks
				executor.getPool().execute(() -> executor.runnableUrgentTask());
				executor.getPool().execute(() -> executor.runnableHighTask());
				executor.getPool().execute(() -> executor.runnableMediumTask());
				executor.getPool().execute(() -> executor.runnableLowTask());
				return;
		}
	}
	
	public void runnableLowTask() {
		int timer = Priority.LOW.getTimer();
		int count = 1;
		while(RequestDAOImpl.pending.size() > 0) {
			for(Request r : RequestDAOImpl.pending) {
				Status current = r.getStatus();
				if(r.getSLA() == Priority.LOW  && (current == Status.NEW || current == Status.IN_PROGRESS)) {
					// request is low priority and approval in progress
					String approver = r.getNextApprover();
					if(!approver.equals(r.getRequestor())) {	// next reviewer is not requestor aka not in document requested phase
						Employee next_approver = empDao.get(approver);
						try {
							System.out.println("Waiting for " + r.getNextApprover() + " to review request");
							Thread.sleep(timer);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						String comment;
						if(current == r.getStatus() && approver.equals(r.getNextApprover())) {
							// Status is the same as before sleep so escalate
							
							if(next_approver.getDept().getName() != Team.BENEFITS && next_approver.getRole() != Role.COORDINATOR) {
								
								comment = "Auto-approval initiated. " + next_approver.getUsername() + " took too long to respond";
								reqServ.setNextApprover(r, empDao.search(r.getRequestor()));
								next_approver = empDao.search(r.getNextApprover());
								reqServ.addRequest(r.getReqID(), next_approver);
							
								if(r.getComment() != null || !r.getComment().equals("")) {
									r.getCommHistory().append(System.getProperty("line.separator"));
									r.getCommHistory().append(r.getComment());
								}
								next_approver.setMessage(comment);
								r.setComment(comment);
								r.setStatus(Status.IN_PROGRESS);
								
								reqDao.update(r, empDao.search(r.getRequestor()));
								empDao.update(next_approver);
								System.out.println(comment);
							}
							
							else {	// Send reminder email to BenCo & Manager
								Employee mgr = empDao.search(next_approver.getManager());
								comment = "Request [ID: " + r.getReqID() + "] has been pending approval from " + next_approver.getUsername() + ". "
										+ mgr.getUsername() + " please ensure it is reviewed ASAP (Reminder: " + count + ")";
																				
								if(r.getComment() != null || !r.getComment().equals("")) {
									r.getCommHistory().append(System.getProperty("line.separator"));
									r.getCommHistory().append(r.getComment());
								}
								
								r.setComment(comment);
								next_approver.setMessage(comment);
								mgr.setMessage(comment);
								
								count++;
								
								reqDao.update(r, empDao.search(r.getRequestor()));
								empDao.update(next_approver);
								empDao.update(mgr);
								System.out.println(comment);
							}				
						}
					}
				}
			}
		}
	}
	
	public void runnableMediumTask() {
		int timer = Priority.MEDIUM.getTimer();
		int count = 1;
		
		while(RequestDAOImpl.pending.size() > 0) {
			for(Request r : RequestDAOImpl.pending) {
				Status current = r.getStatus();
				if(r.getSLA() == Priority.MEDIUM  && (current == Status.NEW || current == Status.IN_PROGRESS)) {
					// request is medium priority and approval in progress
					String approver = r.getNextApprover();
					if(!approver.equals(r.getRequestor())) {	// next reviewer is not requestor aka not in document requested phase
						Employee next_approver = empDao.get(approver);
						try {
							System.out.println("Waiting for " + r.getNextApprover() + " to review request");
							Thread.sleep(timer);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						String comment;
						if(current == r.getStatus() && approver.equals(r.getNextApprover())) {
							// Status is the same as before sleep so escalate
							
							if(next_approver.getDept().getName() != Team.BENEFITS && next_approver.getRole() != Role.COORDINATOR) {
								
								comment = "Auto-approval initiated. " + next_approver.getUsername() + " took too long to respond";
								reqServ.setNextApprover(r, empDao.search(r.getRequestor()));
								next_approver = empDao.search(r.getNextApprover());
								
								if(r.getComment() != null || !r.getComment().equals("")) {
									r.getCommHistory().append(System.getProperty("line.separator"));
									r.getCommHistory().append(r.getComment());
								}
								
								next_approver.setMessage(comment);
								r.setComment(comment);
								r.setStatus(Status.IN_PROGRESS);
	
								reqDao.update(r, empDao.search(r.getRequestor()));
								empDao.update(next_approver);
								System.out.println(comment);
							}
							
							else {	// Send reminder email to BenCo & Manager
								Employee mgr = empDao.search(next_approver.getManager());
								comment = "Request [ID: " + r.getReqID() + "] has been pending approval from " + next_approver.getUsername() + ". "
										+ mgr.getUsername() + " please ensure it is reviewed ASAP (Reminder: " + count + ")";
																		
								if(r.getComment() != null || !r.getComment().equals("")) {
									r.getCommHistory().append(System.getProperty("line.separator"));
									r.getCommHistory().append(r.getComment());
								}
								
								r.setComment(comment);
								next_approver.setMessage(comment);
								mgr.setMessage(comment);
								
								count++;
								
								reqDao.update(r, empDao.search(r.getRequestor()));
								empDao.update(next_approver);
								empDao.update(mgr);
								System.out.println(comment);
							}							
						}
					}
				}
			}
		}
	}
	
	public void runnableHighTask() {
		int timer = Priority.HIGH.getTimer();
		int count = 1;
		
		while(RequestDAOImpl.pending.size() > 0) {
			for(Request r : RequestDAOImpl.pending) {
				Status current = r.getStatus();
				if(r.getSLA() == Priority.HIGH  && (current == Status.NEW || current == Status.IN_PROGRESS)) {
					// request is high priority and approval in progress
					String approver = r.getNextApprover();
					if(!approver.equals(r.getRequestor())) {	// next reviewer is not requestor aka not in document requested phase
						Employee next_approver = empDao.get(approver);
						try {
							System.out.println("Waiting for " + r.getNextApprover() + " to review request");
							Thread.sleep(timer);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						String comment;
						if(current == r.getStatus() && approver.equals(r.getNextApprover())) {
							// Status is the same as before sleep so escalate
							
							if(next_approver.getDept().getName() != Team.BENEFITS && next_approver.getRole() != Role.COORDINATOR) {
								
								comment = "Auto-approval initiated. " + next_approver.getUsername() + " took too long to respond";
								reqServ.setNextApprover(r, empDao.search(r.getRequestor()));
								next_approver = empDao.search(r.getNextApprover());
								
								if(r.getComment() != null || !r.getComment().equals("")) {
									r.getCommHistory().append(System.getProperty("line.separator"));
									r.getCommHistory().append(r.getComment());
								}
								
								next_approver.setMessage(comment);
								r.setComment(comment);
								r.setStatus(Status.IN_PROGRESS);
	
								reqDao.update(r, empDao.search(r.getRequestor()));
								empDao.update(next_approver);
								System.out.println(comment);
							}
							
							else {	// Send reminder email to BenCo & Manager
								Employee mgr = empDao.search(next_approver.getManager());
								comment = "Request [ID: " + r.getReqID() + "] has been pending approval from " + next_approver.getUsername() + ". "
										+ mgr.getUsername() + " please ensure it is reviewed ASAP (Reminder: " + count + ")";
															
								if(r.getComment() != null || !r.getComment().equals("")) {
									r.getCommHistory().append(System.getProperty("line.separator"));
									r.getCommHistory().append(r.getComment());
								}
								
								r.setComment(comment);
								next_approver.setMessage(comment);
								mgr.setMessage(comment);
								
								count++;
								
								reqDao.update(r, empDao.search(r.getRequestor()));
								empDao.update(next_approver);
								empDao.update(mgr);
								System.out.println(comment);
							}
							
						}
					}
				}
			}	
		}
	}
	
	public void runnableUrgentTask() {
		int timer = Priority.URGENT.getTimer();
		int count = 1;
		
		while(RequestDAOImpl.pending.size() > 0) {
			for(Request r : RequestDAOImpl.pending) {
				Status current = r.getStatus();
				if(r.getSLA() == Priority.URGENT  && (current == Status.NEW || current == Status.IN_PROGRESS)) {
					// request is urgent priority and approval in progress
					String approver = r.getNextApprover();
					if(!approver.equals(r.getRequestor())) {	// next reviewer is not requestor aka not in document requested phase
						Employee next_approver = empDao.get(approver);
						try {
							System.out.println("Waiting for " + r.getNextApprover() + " to review request");
							Thread.sleep(timer);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
						String comment;
						if(current == r.getStatus() && approver.equals(r.getNextApprover())) {
							// Status is the same as before sleep so escalate
							
							if(next_approver.getDept().getName() != Team.BENEFITS && next_approver.getRole() != Role.COORDINATOR) {
								
								comment = "Auto-approval initiated. " + next_approver.getUsername() + " took too long to respond";
								reqServ.setNextApprover(r, empDao.search(r.getRequestor()));
								next_approver = empDao.search(r.getNextApprover());
								
								if(r.getComment() != null || !r.getComment().equals("")) {
									r.getCommHistory().append(System.getProperty("line.separator"));
									r.getCommHistory().append(r.getComment());
								}
								
								next_approver.setMessage(comment);
								r.setComment(comment);
								r.setStatus(Status.IN_PROGRESS);
	
								reqDao.update(r, empDao.search(r.getRequestor()));
								empDao.update(next_approver);
								System.out.println(comment);
							}
							
							else {	// Send reminder email to BenCo & Manager
								Employee mgr = empDao.search(next_approver.getManager());
								comment = "Request [ID: " + r.getReqID() + "] has been pending approval from " + next_approver.getUsername() + ". "
										+ mgr.getUsername() + " please ensure it is reviewed ASAP (Reminder: " + count + ")";
																		
								if(r.getComment() != null || !r.getComment().equals("")) {
									r.getCommHistory().append(System.getProperty("line.separator"));
									r.getCommHistory().append(r.getComment());
								}
								
								r.setComment(comment);
								next_approver.setMessage(comment);
								mgr.setMessage(comment);
								
								count++;
								
								reqDao.update(r, empDao.search(r.getRequestor()));
								empDao.update(next_approver);
								empDao.update(mgr);
								System.out.println(comment);
							}							
						}
					}
				}
			}
		}
	}
}
