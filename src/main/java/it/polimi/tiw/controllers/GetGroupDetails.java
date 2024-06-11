package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
import it.polimi.tiw.dao.UserDAO;
import it.polimi.tiw.utils.ConnectionHandler;

@WebServlet("/GetGroupDetails")
public class GetGroupDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;



    public GetGroupDetails() {
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


	@Override
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


		// get and check params
		Integer groupID = null;
		try {
			groupID = Integer.parseInt(request.getParameter("groupid"));
		} catch (NumberFormatException | NullPointerException e) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
			return;
		}

		// Retrieve all the infos given GroupID
		GroupDAO groupDAO = new GroupDAO(connection);
		Group myGroup = null;
		List<User> invitedUsers = new ArrayList<>();

		try {
			myGroup = groupDAO.getGroupByID(groupID);
			if(myGroup == null) throw new Exception("Group recovered but null");
		} catch (SQLException e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover Group");
			return;
		} catch (Exception e) {
			e.printStackTrace();
			response.sendError(HttpServletResponse.SC_NOT_FOUND, "Group recovered but null");
			return;
		}

		if(myGroup.getParticipants().size() > 0) {
			try {
				UserDAO userDAO = new UserDAO(connection);
				User user1 = null;

				for(String username : myGroup.getParticipants()) {
					user1 = userDAO.getUserByUsername(username);
					invitedUsers.add(user1);
				}

				// Adding creator to the list for better displaying
				invitedUsers.add(0, userDAO.getUserByUsername(myGroup.getCreator()));


			}catch (SQLException e) {
				e.printStackTrace();
				response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover invited Users");
				return;
			}
		}



    	// Redirect to the Group Detail page
    	// and add Group to the parameters!!

		String path = "/WEB-INF/DettagliGruppo.html";
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("group", myGroup);
		ctx.setVariable("users", invitedUsers);
		templateEngine.process(path, ctx, response.getWriter());

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
