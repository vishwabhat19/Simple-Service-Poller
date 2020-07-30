Critical issues (required to complete the assignment):

- Whenever the server is restarted, any added services disappear
- There's no way to delete individual services
- We want to be able to name services and remember when they were added
- The HTTP poller is not implemented

I have solved the critical issues by making sure that the services are added to the SQLLITE database and then fetched from there.
Implemented the removeService() method and rest service to remove a service by passing its parameter:  http://localhost:8080/service/{serviceName}
Added a Date column and name column to remember the name and time when services were added.
Implemente the HTTP poller to check for some common success HTTP statuses.

Frontend/Web track:
- We want full create/update/delete functionality for services - Completed this in react
- The results from the poller are not automatically shown to the user (you have to reload the page to see results) - This happens now since I update the state and the component renders itself.
- We want to have informative and nice looking animations on add/remove services - Implemented an alert.