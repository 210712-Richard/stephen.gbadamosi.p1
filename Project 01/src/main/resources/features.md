# Tuition Reimbursement Management System for Spark Productions

## Employee Stories

** Employee Access **
* I can login to the platform
* I can update account/contact information
* I can view my reimbursement stipend balance
* I can view my reimbursement request history and status
* I can create a new reimbursement request (complete reimbursement form)
* I can perform status checks on active requests pending approval
* I can upload supporting documents for reimbursement requests

** Supervisor Access **
* I can perform all employee access operations and..
* Request addition documentation for reimbursement approval
* Approve or Deny reimbursement requests
* Request auto-approved and sent to Department Head if response not received in a timely manner

** Department Head Access **
* I can perform all employee & supervisor access operations and..
* Skip approval for department head access if requesting employee is direct report
* Request auto-approved and sent to Benefits Coordinator if response not received in a timely manner

** BenCo Access **
* I can perform all operations Department Head to employee operations 
* I can alter the employee reimbursement amount and must provide a reason
* Must confirm passing grade for Academic reimbursements and can review supporting documents for all reimbursements
* Approval from BenCo cannot be skipped
* Response delays from BenCo should result in email escalation to direct supervisor

** Additional Notes **
* If the BenCo changes the reimbursement amount, the Employee should be notified and given the option to cancel the request.  
* The BenCo is allowed to award an amount larger than the amount available for the employee.  
* The BenCo must provide reason for this, and the reimbursement must be marked as exceeding available funds for reporting purposes.
* Upon completion of the event, the employee should attach either the Grade or Presentation as appropriate.  
* After upload of a grade, the BenCo must confirm that the grade is passing.  
* After upload of a presentation, the direct manager must confirm that the presentation was satisfactory and presented to the appropriate parties.  Upon confirmation, the amount is awarded to the requestor.
* Only interested parties should be able to access the grades/presentations.  Interested parties include the requestor and approvers. 

## Data Design Pattern
* Employee
* Request - Tuition Reimbursement Form
* EmployeeService
* Request Service
* CassandraUtil
* DataBaseInstatiator

## Database Tables
* Employees
* Request
* 