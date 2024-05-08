package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/RegisterController")
public class RegisterController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

    public RegisterController() {
        super();
    }


	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
//		ServletContext servletContext = getServletContext();
//		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
//		templateResolver.setTemplateMode(TemplateMode.HTML);
//		this.templateEngine = new TemplateEngine();
//		this.templateEngine.setTemplateResolver(templateResolver);
//		templateResolver.setSuffix(".html");
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        
		String username = null;
        String email = null;
    	String name = null;
    	String surname = null;
        String password = null;
        String confirmPassword = null;
		
		try { // StringEscapeUtils.escapeJava();
			username = request.getParameter("username");
			email = request.getParameter("email");
			name = request.getParameter("name");
			surname = request.getParameter("surname");
			password = request.getParameter("password");
			confirmPassword = request.getParameter("confirmPassword");
			
			if (username == null || name == null || surname == null || email == null || password == null || confirmPassword == null 
					|| username.isEmpty() || name.isEmpty() || surname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
				throw new Exception("Missing or empty credential value");
			} else if(!password.equals(confirmPassword)){
				throw new Exception("Non-matching passwords");
			}
		} catch (Exception e) {
			// for debugging only e.printStackTrace();
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing or incorrect credential value");
			return;
		}
		
		
		// query the DB for checking that username and email are Unique
        UserDAO userDao = new UserDAO(connection);
        boolean isUnique;
        try {
        	isUnique = userDao.usernameEmailUnique(username, email);
        	if(!isUnique)
        		throw new Exception("Non-unique username or password");
        } catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to check unicity of username and email");
			return;
		} catch (Exception e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Non-unique username or password");
			return;
		}
        
        // insert into the DB the new User!
        User user = null;
        boolean success;
        try {
    		user = new User(username, name, surname, email, password);
   			success = userDao.registerSuccess(user);
   			
    		if(success) {
    			// REGISTRATION SUCCESS message
    			
//    			ServletContext servletContext = getServletContext();
//    			final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
//    			ctx.setVariable("errorMsg", "Incorrect username or password");
//    			path = "/LandingPage.html";
//    			templateEngine.process(path, ctx, response.getWriter());
    			
    			response.sendRedirect("/LandingPage.html");
    		}
    		
    		// if not successful, throws sql exception
    			
   		} catch (SQLException e) {
   			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not Possible to register User");    			
   			return;
    	}

	}
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}
