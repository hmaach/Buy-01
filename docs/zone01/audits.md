
================================================================
# mr-jenk
Source: https://learn.zone01oujda.ma//api/content/root/01-edu_module/content/mr-jenk/audit/README.md
================================================================

#### Functional

##### Download the project and trigger a Jenkins build. Observe if the pipeline runs as expected.

###### Does the pipeline initiate and run successfully from start to finish?

##### Trigger some intentional build errors and observe Jenkins' response.

###### Does Jenkins respond appropriately to build errors?

##### Examine the automated testing step.

###### Are tests run automatically during the pipeline execution? Does the pipeline halt on test failure?

##### Make a minor change in the source code, commit, and push. Observe if the pipeline is triggered automatically.

###### Does a new commit and push automatically trigger the Jenkins pipeline?

##### Check the deployment process.

###### Is the application deployed automatically after a successful build? Is there a rollback strategy in place?

#### Security

##### Examine the permissions on the Jenkins dashboard.

###### Are permissions set appropriately to prevent unauthorized access or changes?

##### Review how sensitive data (like API keys, passwords) is managed in Jenkins.

###### Is sensitive data secured using Jenkins secrets or environment variables?

#### Code Quality and Standards

##### Examine the Jenkinsfile or the build configuration.

###### Is the code/script well-organized and understandable? Are there any best practices being ignored?

##### Look into the test report formats and outputs.

###### Are test reports clear, comprehensive, and stored for future reference?

##### Check for notifications setup.

###### Are notifications triggered on build and deployment events? Are they informative?

#### Bonus

##### Examine if parameterized builds are implemented.

###### +Are there options for customizing the build run with different parameters?

##### Examine the distributed builds (if implemented).

###### +Are multiple agents utilized effectively for distributed builds?

================================================================
# safe-zone
Source: https://learn.zone01oujda.ma//api/content/root/01-edu_module/content/safe-zone/audit/README.md
================================================================

#### Functional

##### Access the SonarQube web interface running on your local environment.

###### Is the SonarQube web interface accessible, and has it been configured to work with your project's codebase?

##### Integrate SonarQube with your GitHub repository.

###### Is SonarQube integrated with GitHub, and does it trigger code analysis on every push to the repository?

##### Set up and configure SonarQube for code analysis using Docker.

###### Is SonarQube configured correctly, and does it analyze code during the CI/CD pipeline?

##### Automate code analysis using SonarQube during the CI/CD pipeline.

###### Does the CI/CD pipeline correctly analyze code, and does it fail when code quality or security issues are detected?

##### Implement a code review and approval process.

###### Is there a code review and approval process in place to ensure code quality improvements are reviewed and approved?

#### Comprehension

##### SonarQube Setup and Integration

###### Can the student explain the steps required to set up SonarQube within the project environment?

###### Can the student describe the process of integrating SonarQube with the project's CI/CD pipeline and GitHub repository?

##### SonarQube Functionality

###### Can the student explain how SonarQube functions within the project, including its role in code analysis and how it contributes to code quality improvement?

#### Security

##### Review the permissions and access controls in SonarQube.

###### Are permissions set appropriately to prevent unauthorized access to code analysis results?

#### Code Quality and Standards

##### Examine the SonarQube rules and code analysis reports.

###### Are SonarQube rules configured correctly, and are code quality and security issues accurately identified?

##### Review any code quality improvements made based on SonarQube feedback.

###### Are code quality issues addressed and committed to the GitHub repository?

#### Bonus

##### Set up email or Slack notifications for code analysis results.

###### +Are notifications in place for code analysis results?

##### Integrate SonarQube with IDEs (Integrated Development Environments).

###### +Are IDE integrations in place to provide developers with real-time code quality feedback during development?


================================================================
# buy-02
Source: https://learn.zone01oujda.ma//api/content/root/01-edu_module/content/buy-02/audit/README.md
================================================================

#### Functional

##### Verify that the necessary tables, fields, relations are added.

###### Has the database design been correctly implemented?

###### Have the students added new relationships and have they used them correctly?

###### Did the students convince you with their additions to the database?

##### Review the project repository to check for PRs and code reviews.

###### Are developers following a collaborative development process with PRs and code reviews?

##### Check the implementation of Orders MicroService, User Profile, Search and Filtering, and Shopping Cart functionalities.

###### Are the implemented functionalities consistent with the project instructions?

###### Are the implemented functionalities clean and do they not pop up any errors or warnings in both back and front end?

##### Add products to the shopping cart and refresh the page.

###### Are the added products still in the shopping cart with the selected quantities?

##### Utilize SonarQube to assess code quality and check for improvements based on SonarQube feedback.

###### Are code quality issues identified by SonarQube being addressed and fixed?

##### Review the user interface to ensure it's user-friendly and responsive.

###### Does the application provide a seamless and responsive user experience?

##### Check if proper error handling and validation mechanisms are in place.

###### Are user interactions handled gracefully with appropriate error messages?

##### Verify the implementation of security measures as specified in the project instructions.

###### Are security measures consistently applied throughout the application?

#### Collaboration and Development Process

##### Check the repository's PR history and comments to ensure code reviews are conducted.

###### Are code reviews being performed for each PR?

##### Inspect the CI/CD pipeline configuration with Jenkins to ensure automated builds, tests, and deployments.

###### Is the CI/CD pipeline correctly set up and being utilized for PRs?

##### Examine the repository log and PR merges to ensure that branches are being merged as instructed.

###### Are branches merged correctly, and is the main codebase up-to-date?

##### Run a full test of the application to assess functionality and identify any issues.

###### Does the application pass a comprehensive test to ensure that all new features work as expected?

##### Inspect the codebase for unit tests related to different parts of the application.

###### Are there unit tests in place for critical parts of the application?

#### Bonus

##### Verify if the wishlist feature, if implemented, functions correctly.

###### +Is the wishlist feature functioning as expected?

##### Check if different payment methods, if implemented, work as intended.

###### +Are the implemented payment methods functioning correctly?


================================================================
# nexus
Source: https://learn.zone01oujda.ma//api/content/root/01-edu_module/content/nexus/audit/README.md
================================================================

#### Functional

##### Setup Nexus Repository Manager

###### Has the Nexus Repository Manager been successfully installed and configured on a local or remote server?

###### Is Nexus configured correctly to work under the specified user not 'root' user?

###### Are repositories set up for different artifact types such as JARs, WARs, and Docker images?

##### Development and Structure

###### Is there a simple web application developed using the Spring Boot framework?

###### Does the project utilize a proper Maven or Gradle project structure?

##### Artifact Publishing

###### Is the build tool (Maven or Gradle) properly configured to publish built artifacts (JARs/WARs) to the relevant repositories in Nexus?

##### Dependency Management

###### Is Nexus used as a proxy for fetching external dependencies required by the web application?

###### Is the project configured to resolve dependencies from Nexus repositories?

##### Versioning

###### Is versioning implemented for the web application and its artifacts using Nexus capabilities?

###### Are different versions of artifacts effectively retrieved and managed?

##### Docker Integration

###### Is there a Docker repository set up in Nexus, and is the Docker image published to the repository?

##### Continuous Integration (CI)

###### Does the pipeline automatically trigger builds, tests, and artifact publishing upon repository changes?

##### Documentation

###### Is clear and detailed documentation provided for project setup, configuration, and usage?

###### Does the documentation include relevant screenshots and examples?

#### Bonus: Nexus Security and Access Control

##### Security Exploration

###### +Have Nexus security features like user authentication and role-based access control been explored?

###### +Are repository-level permissions effectively configured?

##### Configuration

###### +Are security settings configured to restrict access to specific artifacts or repositories in Nexus?

