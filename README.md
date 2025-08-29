Hiring Solution - Spring Boot app

Overview
- On startup the app calls the provided generateWebhook endpoint, receives a webhook URL and accessToken, computes the SQL solution for Question 1 (odd reg no), stores the query locally, and posts the final query to the returned webhook with the Authorization header set to the token.

**Result:**
SELECT 
    p.AMOUNT as SALARY,
    CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) as NAME,
    YEAR(CURRENT_DATE) - YEAR(e.DOB) as AGE,
    d.DEPARTMENT_NAME
FROM PAYMENTS p
JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID
JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID
WHERE DAY(p.PAYMENT_TIME) != 1
ORDER BY p.AMOUNT DESC
LIMIT 1;

Success Response:
{"success":true,"message":"Webhook processed successfully"}

Execution Details: 
Registration: 22BCB7109
Question Type: Question 1 (09 is odd) 
Problem: Find highest salary NOT credited on 1st day of month JWT Token: Successfully received and used 
Submission: "Webhook processed successfully" What This Query Does: Joins three tables: PAYMENTS → EMPLOYEE → DEPARTMENT Filters transactions: WHERE DAY(p.PAYMENT_TIME) != 1 (excludes 1st day) 
Finds highest salary: ORDER BY p.AMOUNT DESC LIMIT 1 Returns required columns: SALARY: Payment amount NAME: Combined first + last name AGE: Calculated from DOB DEPARTMENT_NAME: Department name



