package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/Anagrafica")
public class AnagraficaController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       
    public AnagraficaController() {
        super();
    }
    

	public void init() throws ServletException {
		connection  = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		// ---------------------- SESSION CHECK ------------------------
    	// If the user is not logged in (not present in session) redirect to the login
    	String loginpath = getServletContext().getContextPath() + "/LandingPage.html";
    	HttpSession session = request.getSession();
    	if (session.isNew() || session.getAttribute("user") == null) {
    		response.sendRedirect(loginpath);
    		return;
    	}
    	User user = (User) session.getAttribute("user");
    	// End of Session persistency check
		
		// Counter check
    	Integer counter = (Integer) session.getAttribute("counter");
    	if(counter < 3 ) {
    		counter++;
    		session.setAttribute("counter", counter);;
    	} else {
    		
    		
    		// destroy session.Params
    		
    		
//    		String ctxpath = getServletContext().getContextPath();
    		//	String path = ctxpath + "/Anagrafica";
    		response.sendRedirect("/Cancellazione");
    	}
    	
    	
    	// forse si possono ottenere dalla request 
    	
		String title = (String) session.getAttribute("title");
		Date startDate = (Date) session.getAttribute("date");
		Integer duration = (Integer) session.getAttribute("duration");
		Integer minParts = (Integer) session.getAttribute("minParts");
		Integer maxParts = (Integer) session.getAttribute("maxParts");
		String creator = user.getUsername();
		
		if (title == null || startDate == null || duration == null || minParts == null || maxParts == null || creator == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
			return;
		}
	
		UserDAO userDAO = new UserDAO(connection);
		List<User> usersExceptCreator = null;
		try {
			usersExceptCreator = userDAO.findAllUsersExcept(creator);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover all users");
			return;
		}
		
		
		
		// set of session attributes
		
		
		
	}


	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		
		// ---------------------- SESSION CHECK ------------------------
    	// If the user is not logged in (not present in session) redirect to the login
    	String loginpath = getServletContext().getContextPath() + "/LandingPage.html";
    	HttpSession session = request.getSession();
    	if (session.isNew() || session.getAttribute("user") == null) {
    		response.sendRedirect(loginpath);
    		return;
    	}
    	User user = (User) session.getAttribute("user");
    	// End of Session persistency check
		
    	
    	String[] selectedUsers = request.getParameterValues("selectedUsers");
    	
    	// CONTROL & REDIRECT POLICIES
    	
    	
		
//		// Create Group in DB
//		GroupDAO groupDAO = new GroupDAO(connection);
//		try {
//			// groupDAO.createGroup(title, startDate, duration, minParts, maxParts, user.getUsername(), null);
//			
//		} catch (SQLException e) {
//			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to create new Group");
//			return;
//		}

	}
	
	
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
