Hiring Solution - Spring Boot app

Overview
- On startup the app calls the provided generateWebhook endpoint, receives a webhook URL and accessToken, computes the SQL solution for Question 1 (odd reg no), stores the query locally, and posts the final query to the returned webhook with the Authorization header set to the token.

Files added
- `pom.xml` - Maven build file
- `src/main/java/...` - Spring Boot application and runner that performs the flow
- `sql/data.sql` - DDL and INSERTs for the three tables
- `sql/final_query.sql` - The final SQL query that solves Question 1
- `solutions/final_query.sql` - This file is created at runtime when the app runs

Final SQL Query (also in `sql/final_query.sql`):

SELECT p.amount AS SALARY,
       CONCAT(e.first_name, ' ', e.last_name) AS NAME,
       TIMESTAMPDIFF(YEAR, e.dob, CURDATE()) AS AGE,
       d.department_name AS DEPARTMENT_NAME
FROM payments p
JOIN employee e ON p.emp_id = e.emp_id
JOIN department d ON e.department = d.department_id
WHERE DAY(p.payment_time) != 1
  AND p.amount = (
    SELECT MAX(amount) FROM payments WHERE DAY(payment_time) != 1
  );

Build & Run (Windows PowerShell)

# From workspace root (where this README is)
cd .\hiring-solution
mvn clean package
# Run the generated JAR (after build)
java -jar target\hiring-solution-0.0.1-SNAPSHOT.jar

Notes & Submission
- I chose Option 1: I created the project here. To deploy to GitHub, create a repo and push the `hiring-solution` folder.
- The submission requires the JAR in the repo. If you build locally (using the commands above) commit `target/hiring-solution-0.0.1-SNAPSHOT.jar` and push.

Suggested git commands to push (run locally):

# initialize and push
cd hiring-solution
git init
git add .
git commit -m "Add hiring solution Spring Boot project"
# create repo on GitHub (use your account) then add remote and push
git remote add origin https://github.com/your-username/your-repo.git
git branch -M main
git push -u origin main

Then upload the raw JAR link or add a GitHub Release and provide raw download link in the submission form.
