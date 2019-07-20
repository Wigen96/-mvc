# -mvc
spring mvc的流程：
请求 --> DispatcherServlet --> HandleMapping --> DispatcherServlet --> HandlerAdapter -- > Handler --> HandleAdapter --> DispatcherServlet --> ViewResolver --> DispatcherServlet --> View --> DispatcherServlet --> 返回浏览器             
   
