package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.GroupDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/Home")
public class HomeController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
       

    public HomeController() {
        super();
    }
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
    
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
    
    	
    	
    	GroupDAO groupDAO = new GroupDAO(connection);
    	List<Group> myGroups = new ArrayList<Group>();
    	List<Group> othersGroups = new ArrayList<Group>();
    	
    	try {
			myGroups = groupDAO.findMyGroups(user.getUsername());
			othersGroups = groupDAO.findOthersGroup(user.getUsername());
			if(myGroups == null || othersGroups == null)
				throw new Exception("Groups are null");
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover groups");
			return;
		} catch (Exception e1) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Groups are recovered but null");
			return;
		}
    	
    	
//		 Redirect to the Home page and add Groups to the parameters!!
//		String path = "/WEB-INF/Home.html";
//		ServletContext servletContext = getServletContext();
//		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
//		ctx.setVariable("missions", missions);
//		templateEngine.process(path, ctx, response.getWriter());
    }
	

	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
