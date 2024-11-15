# Jingp_takeaway

#### introduce
WeChat Public Order & Backend Management System:
This project aims to customise a takeaway platform for catering enterprises and users. The management side realises functions such as identity authentication, dish management, package management, data statistics, etc. The user side realises functions such as browsing dishes, paying by WeChat, adding shopping cart, etc. in the mobile applet.

#### details
1. Independently implement the development of 70 interfaces, including 45 on the management side and 25 on the user side, and independently solve the problems encountered in the development.
2. Login and authentication using JWT token technology, complete user authentication with a custom interceptor, through the ThreadLocal with interceptor for Token verification, determine whether the user is in the login state, and solve the problem of stateless HTTP requests.
3. Use Redis to cache high-frequency request data such as shop business status, and use SpringCache to optimise the code to improve system performance and response speed.
4. Use Nginx as an HTTP server, deploy static resources, and implement reverse proxy and load balancing.
5. Use SpringTask to implement timed processing of order status, automatic cancellation of orders after timeout and other functions.
