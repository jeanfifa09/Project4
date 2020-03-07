/* Name: Jordan Germinal
 * Course: CNT 4714 Fall 2019
 * Project Four Assignment title: A Three-Tier Distributed Web-Based Application
 *Date: December 1st, 2019
*/
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author jean0
 */
@WebServlet(urlPatterns = {"/SQLQueryServlet"})
public class SQLQueryServlet extends HttpServlet {

    private Connection connection;
	private Statement statement;

	// init: setup database connection
	@Override
	public void init(ServletConfig config) throws ServletException {
		// override config
		super.init(config);
		// connected to DB
		try {
			// using xml file
			Class.forName("com.mysql.jdbc.Driver");  

			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/project4",
					"root", "");
			statement = connection.createStatement();
		}

		catch (Exception e) {
			e.printStackTrace();
			throw new UnavailableException(e.getMessage());
		}

	}
    
    
    
    
    
    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet SQLQueryServlet</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet SQLQueryServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    // process get request from jsp front-end
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		String textBox = request.getParameter("textBox");
		String textBoxLowerCase = textBox.toLowerCase();
		String result = null;
		
		//check to see if it is a select statement
		if (textBoxLowerCase.contains("select")) {

			try {
				result = doSelectQuery(textBoxLowerCase);
			} catch (SQLException e) {
				result = "<span>" + e.getMessage() + "</span>";

				e.printStackTrace();
			}
		}
		else { //do insert,update, delete, create, drop
			try {
				result = doUpdateQuery(textBoxLowerCase);
			}catch(SQLException e) {
				result = "<span>" + e.getMessage() + "</span>";

				e.printStackTrace();
			}
		}

		HttpSession session = request.getSession();
		session.setAttribute("result", result);
		session.setAttribute("textBox", textBox);
		RequestDispatcher dispatcher = getServletContext().getRequestDispatcher("/index.jsp");
		dispatcher.forward(request, response);
	}

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
       doGet(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

    
  // execute a select query and create table html with resultset
	public String doSelectQuery(String textBox) throws SQLException {
		String result;
		// run sql command
		ResultSet table = statement.executeQuery(textBox);
		// process query results

		ResultSetMetaData metaData = table.getMetaData();
		// table columns html
		int numOfColumns = metaData.getColumnCount();
		// html table openig html
		String tableOpeningHTML = "<div class='container-fluid'><div class='row justify-content-center'><div class='table-responsive-sm-10 table-responsive-md-10 table-responsive-lg-10'><table class='table'>";
		// table html columns
		String tableColumnsHTML = "<thead class='thead-dark'><tr>";
		for (int i = 1; i <= numOfColumns; i++) {
			tableColumnsHTML += "<th scope='col'>" + metaData.getColumnName(i) + "</th>";
		}

		tableColumnsHTML += "</tr></thead>"; // close the html tale column element

		// table html body/rows
		String tableBodyHTML = "<tbody>";
		// get row info
		while (table.next()) {
			tableBodyHTML += "<tr>";
			for (int i = 1; i <= numOfColumns; i++) {
				// if first element
				if (i == 1)
					tableBodyHTML += "<td scope'row'>" + table.getString(i) + "</th>";
				else
					tableBodyHTML += "<td>" + table.getString(i) + "</th>";
			}
			tableBodyHTML += "</tr>";
		}

		tableBodyHTML += "</tbody>";

		// closing html
		String tableClosingHTML = "</table></div></div></div>";
		result = tableOpeningHTML + tableColumnsHTML + tableBodyHTML + tableClosingHTML;

		return result;
	}
	
	private String doUpdateQuery(String textBoxLowerCase) throws SQLException {
		String result = null;
		int numOfRowsUpdated = 0;
		
		//get number of shipment with quantity >= 100 before update/insert
		ResultSet beforeQuantityCheck = statement.executeQuery("select COUNT(*) from shipments where quantity >= 100");
		beforeQuantityCheck.next();
		int numOfShipmentsWithQuantityGreaterThan100Before = beforeQuantityCheck.getInt(1);
		
		//create temp table for the case of updating suppliers status's 
		statement.executeUpdate("create table shipmentsBeforeUpdate like shipments");
		//copy table over to new temp table
		statement.executeUpdate("insert into shipmentsBeforeUpdate select * from shipments");
		
		//execute update
		numOfRowsUpdated = statement.executeUpdate(textBoxLowerCase);
		result = "<div> The statement executed succesfully.</div><div>" + numOfRowsUpdated + " row(s) affected</div>";
		
		//get number of shipment with quantity >= 100 before update/insert
		ResultSet afterQuantityCheck = statement.executeQuery("select COUNT(*) from shipments where quantity >= 100");
		afterQuantityCheck.next();
		int numOfShipmentsWithQuantityGreaterThan100After = afterQuantityCheck.getInt(1);
		
		result += "<div>" + numOfShipmentsWithQuantityGreaterThan100Before + " < " + numOfShipmentsWithQuantityGreaterThan100After + "</div>";
		
		//update the status of suppliers if shipment quantity is > 100
		if(numOfShipmentsWithQuantityGreaterThan100Before < numOfShipmentsWithQuantityGreaterThan100After) {
			//increase suppliers status by 5
			//handle updates into shipments by using a left join with shipments and temp table
			int numberOfRowsAffectedAfterIncrementBy5 = statement.executeUpdate("update suppliers set status = status + 5 where snum in ( select distinct snum from shipments left join shipmentsBeforeUpdate using (snum, pnum, jnum, quantity) where shipmentsBeforeUpdate.snum is null)");
			result += "<div>Business Logic Detected! - Updating Supplier Status</div>";
			result += "<div>Business Logic Updated " + numberOfRowsAffectedAfterIncrementBy5 + " Supplier(s) status marks</div>";
		}
		
		//drop temp table
		statement.executeUpdate("drop table shipmentsBeforeUpdate");
		
		return result;
	}
  
    
    
}
