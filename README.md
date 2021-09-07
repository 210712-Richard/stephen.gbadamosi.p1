# stephen.gbadamosi.p1

### TRMS Application - Spark Productions ###

This Tuition Reimbursement Management System is meant to simplify the reimbursement process
for employees for purchases on reimbursable events (ex Certifications, University Courses, etc)

## Technologies ##
* AWS S3 Integration for File Upload
* RESTful API built with Javalin
* Data persistence through AWS Keystore for Apache Cassandra
* Logging with Log4J2
* Unit testing with JUnit
* Project Dependency Management with Maven
* Parallel Programming for automated approvals with Threads

## Workflow Overview: ##
* Any employee can submit reimbursement requests via RESTful API calls with HTTP methods
* Approvals from {Supervisor/Manager, Department Head & Benefits Coordinator} or subset of
that group depending on employee role is required for reimbursements to be processed
* Approvers can request additional documentation for reimbursement to be processed
* Requestor can attach email approvals to satisfy Manager or Department Head approvals
* Auto-approvals can occur if Manager or Department Head don't respond to request in alloted time
* Auto-approvals do not occur with Benefits Coordinator, instead a notification is sent
* Benefits Coordinators can adjust the reimbursement amount

## Project Requirements: ##
* Cassandra DB setup with AWS Keyspaces
* S3 Buckets and permissions with AWS
* Javalin framework for RESTful API server
* Import as Maven project for logging, testing and other dependencies
