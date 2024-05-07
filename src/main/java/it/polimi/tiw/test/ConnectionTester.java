package it.polimi.tiw.test;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServlet;
import javax.servlet.ServletException;
import java.sql.DriverManager;
import java.io.PrintWriter;
import java.io.IOException;
public class ConnectionTester extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		final String DB_URL = "jdbc:mysql://localhost:3306/dbtest?serverTimezone=UTC";
		final String USER = "root";
		final String PASS = "tiw";
		String result = "Connection worked";
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
			DriverManager.getConnection(DB_URL, USER, PASS);
		} catch (Exception e) {
			result = "Connection failed";
			e.printStackTrace();
		}
		response.setContentType("text/plain");
		PrintWriter out = response.getWriter();
		out.println(result);
		out.close();
	}
}