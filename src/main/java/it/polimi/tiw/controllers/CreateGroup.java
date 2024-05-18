package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.lang.StringEscapeUtils;

import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.GroupDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/CreateGroup")
public class CreateGroup extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

    public CreateGroup() {
        super();
    }

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
		
	}

	private Date getMeYesterday() {
		return new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
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
		
		
    	// Get and parse all parameters from request
    	boolean isBadRequest = false;
		
		String title = null;
		Date startDate = null;
		Integer duration = null;
		Integer minParts = null;
		Integer maxParts = null;
		
		
		try {
			duration = Integer.parseInt(request.getParameter("duration"));
			title = StringEscapeUtils.escapeJava(request.getParameter("title"));
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			java.util.Date utilDate = sdf.parse(request.getParameter("date"));
			startDate = new java.sql.Date(utilDate.getTime());
			minParts = Integer.parseInt(request.getParameter("minParts"));
			maxParts = Integer.parseInt(request.getParameter("maxParts"));
			
			isBadRequest = duration == null || minParts == null || maxParts == null 
					|| duration <= 0 || maxParts < minParts || minParts <= 0
					|| title == null || title.isEmpty() 
					|| getMeYesterday().after(startDate);
		} catch (NumberFormatException | NullPointerException | ParseException e) {
			isBadRequest = true;
			e.printStackTrace();
		}
		if (isBadRequest) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
			return;
		}
		
		
		// set attributes as session. manage them further in Anagrafica
		
		session.setAttribute("title", title);
		session.setAttribute("date", startDate);
		session.setAttribute("duration", duration);
		session.setAttribute("minParts", minParts);
		session.setAttribute("maxParts", maxParts);
		
		// initialize counter
		
		Integer one = 1;
		session.setAttribute("counter", one);
		
		
		// redirect 
		
		String ctxpath = getServletContext().getContextPath();
		String path = ctxpath + "/Anagrafica";
		response.sendRedirect(path);
    		
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}
