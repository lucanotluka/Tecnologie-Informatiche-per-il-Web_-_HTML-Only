package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ServletContextTemplateResolver;

import it.polimi.tiw.beans.Group;
import it.polimi.tiw.beans.User;
import it.polimi.tiw.dao.GroupDAO;
import it.polimi.tiw.utils.ConnectionHandler;


@WebServlet("/Home")
public class HomeController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;


    public HomeController() {
        super();
    }

	@Override
	public void init() throws ServletException {
		ServletContext servletContext = getServletContext();
		ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
		templateResolver.setTemplateMode(TemplateMode.HTML);
		this.templateEngine = new TemplateEngine();
		this.templateEngine.setTemplateResolver(templateResolver);
		templateResolver.setSuffix(".html");
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	private Date getMeYesterday() {
		return new Date(System.currentTimeMillis() - 24 * 60 * 60 * 1000);
	}

	private Date getGroupEndDate(Date creation, Integer duration) {
		return new Date(creation.getTime() + duration * 24 * 60 * 60 * 1000);
	}

    @Override
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
    	List<Group> myGroups = new ArrayList<>();
    	List<Group> othersGroups = new ArrayList<>();

    	// Retrieve the list of MyGroups from the DB!
    	try {
			myGroups = groupDAO.findMyGroups(user.getUsername());
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover myGroups");
			return;
		}


    	// Retrieve the list of OthersGroups from the DB!
    	try {
			othersGroups = groupDAO.findOthersGroup(user.getUsername());
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover othersGroups");
			return;
		}


//    	 Filtering for active groups only
    	Iterator<Group> myGroupsIterator = myGroups.iterator();
    	while (myGroupsIterator.hasNext()) {
    	    Group group = myGroupsIterator.next();
    	    if (getGroupEndDate(group.getCreationDate(), group.getHowManyDays())
    	            .compareTo(getMeYesterday()) <= 0) {
    	        myGroupsIterator.remove();
    	    }
    	}
    	
    	Iterator<Group> othersGroupsIterator = othersGroups.iterator();
    	while (othersGroupsIterator.hasNext()) {
    	    Group group = othersGroupsIterator.next();
    	    if (getGroupEndDate(group.getCreationDate(), group.getHowManyDays())
    	            .compareTo(getMeYesterday()) <= 0) {
    	        othersGroupsIterator.remove();
    	    }
    	}



    	// Redirect to the Home page
    	// and add Groups to the parameters!!

		String path = "/WEB-INF/Home.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("myGroups", myGroups);
		ctx.setVariable("othersGroups", othersGroups);
		templateEngine.process(path, ctx, response.getWriter());
    }


	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	@Override
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}
