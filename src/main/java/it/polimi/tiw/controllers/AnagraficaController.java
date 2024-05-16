package it.polimi.tiw.controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.util.Arrays;
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


@WebServlet("/Anagrafica")
public class AnagraficaController extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;
	private TemplateEngine templateEngine;
       
    public AnagraficaController() {
        super();
    }
    

    public void init() throws ServletException {
			ServletContext servletContext = getServletContext();
			ServletContextTemplateResolver templateResolver = new ServletContextTemplateResolver(servletContext);
			templateResolver.setTemplateMode(TemplateMode.HTML);
			this.templateEngine = new TemplateEngine();
			this.templateEngine.setTemplateResolver(templateResolver);
			templateResolver.setSuffix(".html");
			connection = ConnectionHandler.getConnection(getServletContext());
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
		
		
    	// --------------- Counter check ---------------
    	Integer counter = (Integer) session.getAttribute("counter");
    	if(counter == 2 ) {    		
    		// destroy session.Params
    		session.removeAttribute("title");;
    		session.removeAttribute("date");
    		session.removeAttribute("duration");
    		session.removeAttribute("minParts");
    		session.removeAttribute("maxParts");
    		session.removeAttribute("counter");
    		
    		// redirect to CANCELLAZIONE    		
			String ctxpath = getServletContext().getContextPath();
			String path = ctxpath + "/WEB-INF/Cancellazione.html";
    		response.sendRedirect(path);
    		return;
    	}
    	// ------------- END of counter check -------------
    	
        	 
    	// retrieving of group attributes from session
    	
		String title = (String) session.getAttribute("title");
		Date startDate = (java.sql.Date) session.getAttribute("date");
		Integer duration = (Integer) session.getAttribute("duration");
		Integer minParts = (Integer) session.getAttribute("minParts");
		Integer maxParts = (Integer) session.getAttribute("maxParts");
		String creator = user.getUsername();
		
		if (title == null || startDate == null || duration == null || minParts == null || maxParts == null || creator == null) {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
			return;
		}
	
		Group group = new Group();
		group.setTitle(title);
		group.setCreationDate(startDate);
		group.setHowManyDays(duration);
		group.setMinParts(minParts);
		group.setMaxParts(maxParts);
		
		
		// List of Users for Anagrafica page: will be used by Thymeleaf!
		
		UserDAO userDAO = new UserDAO(connection);
		List<User> users = null;
		try {
			users = userDAO.findAllUsersExcept(creator);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to recover all users");
			return;
		}
		
		
		// Set the context variables to be shown by Thymeleaf
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		ctx.setVariable("users", users);
		ctx.setVariable("group", group);
		
		
		// The non-first time we'll be here, there will be a List of already selected Users.
		if(counter > 1) {
			
			// the array of pre-selected usernames
			String[] invitedUsers = request.getParameterValues("invitedUsers");
			
			// send the invitedUsers to Thymeleaf: if u.username is in invitedUsers, check V
			ctx.setVariable("alreadyInvitedUsers", invitedUsers);
			
		}	
		
		String path = "/WEB-INF/Anagrafica.html";
		templateEngine.process(path, ctx, response.getWriter());
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
		
    	
    	
    	// --------------- CONTROL & REDIRECT POLICIES ------------------
    	

    	Integer howMany = 0;
    	List<String> alreadyInvitedUsers = null;
    	
    	// When Creator has selected some Users
		try {
			alreadyInvitedUsers = Arrays.asList(request.getParameterValues("invitedUsers"));
			howMany = alreadyInvitedUsers.size();
		} catch (Exception e1) {
			// None selected
		}
  
    		
		String title = (String) session.getAttribute("title");
		Date startDate = (Date) session.getAttribute("date");
		Integer duration = (Integer) session.getAttribute("duration");
		Integer minParts = (Integer) session.getAttribute("minParts");
		Integer maxParts = (Integer) session.getAttribute("maxParts");
		String creator = user.getUsername();
    	
		
		// Set the context variables to be shown by Thymeleaf
		ServletContext servletContext = getServletContext();
		final WebContext ctx = new WebContext(request, response, servletContext, request.getLocale());
		
    		// TOO MANY!
    	if(howMany > maxParts-1){
    		Integer toRemove = howMany -1 - maxParts;
    		
    		Integer counter = (Integer) session.getAttribute("counter");
    		counter++;
    		session.setAttribute("counter", counter);
        	
    		
    		// put the invitedUsers into the request parameter
    		request.setAttribute("alreadyInvitedUsers", alreadyInvitedUsers);
    	
			ctx.setVariable("error", "Troppi utenti selezionati! Eliminarne almeno " + toRemove);
    		String path = "/WEB-INF/Anagrafica";
    		templateEngine.process(path, ctx, response.getWriter());
    		return;
    		
    	} 	// TOO FEW! 
    	else if (howMany < minParts-1) {
    		Integer toAdd = minParts -1 - howMany;
    		
    		
    		Integer counter = (Integer) session.getAttribute("counter");
    		counter++;
    		session.setAttribute("counter", counter);
    		
    		
    		// put the invitedUsers into the request parameter
    		request.setAttribute("alreadyInvitedUsers", alreadyInvitedUsers);
    		
			ctx.setVariable("error", "Troppi pochi utenti selezionati! Aggiungerne almeno " + toAdd);
			String path = "/WEB-INF/Anagrafica";
    		templateEngine.process(path, ctx, response.getWriter());
    		return;
   
    	}
    	
    
    	// else:			 OK!
    	
    	// ACTUALLY CONSTRUCT THE DAOs AND SAVE into DB!
    	GroupDAO groupDAO = new GroupDAO(connection);
    		   // (the groups info are above!)
    	
    	try {
			groupDAO.createGroup(title, startDate, duration, minParts, maxParts, creator, alreadyInvitedUsers);
		} catch (SQLException e) {
			response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Not possible to register the Group");
			return;
		}
    	
    	// THEN, REDIRECT TO HOME
    	String path = getServletContext().getContextPath() + "/Home.html";
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
