/*
 * Template JAVA User Interface
 * =============================
 *
 * Database Management Systems
 * Department of Computer Science &amp; Engineering
 * University of California - Riverside
 *
 * Target DBMS: 'Postgres'
 *
 */


import java.sql.DriverManager;
import java.sql.Connection;
import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.ArrayList;
import java.lang.Math;

/**
 * This class defines a simple embedded SQL utility class that is designed to
 * work with PostgreSQL JDBC drivers.
 *
 */
public class Amazon {

   // reference to physical database connection.
   private Connection _connection = null;

   // handling the keyboard inputs through a BufferedReader
   // This variable can be global for convenience.
   static BufferedReader in = new BufferedReader(
                                new InputStreamReader(System.in));

   static int loggedInUserID = -1;
   static String loggedInUserType = "customer";

   /**
    * Creates a new instance of Amazon store
    *
    * @param hostname the MySQL or PostgreSQL server hostname
    * @param database the name of the database
    * @param username the user name used to login to the database
    * @param password the user login password
    * @throws java.sql.SQLException when failed to make a connection.
    */
   public Amazon(String dbname, String dbport, String user, String passwd) throws SQLException {

      System.out.print("Connecting to database...");
      try{
         // constructs the connection URL
         String url = "jdbc:postgresql://localhost:" + dbport + "/" + dbname;
         System.out.println ("Connection URL: " + url + "\n");

         // obtain a physical connection
         this._connection = DriverManager.getConnection(url, user, passwd);
         System.out.println("Done");
      }catch (Exception e){
         System.err.println("Error - Unable to Connect to Database: " + e.getMessage() );
         System.out.println("Make sure you started postgres on this machine");
         System.exit(-1);
      }//end catch
   }//end Amazon

   // Method to calculate euclidean distance between two latitude, longitude pairs. 
   public double calculateDistance (double lat1, double long1, double lat2, double long2){
      double t1 = (lat1 - lat2) * (lat1 - lat2);
      double t2 = (long1 - long2) * (long1 - long2);
      return Math.sqrt(t1 + t2); 
   }
   /**
    * Method to execute an update SQL statement.  Update SQL instructions
    * includes CREATE, INSERT, UPDATE, DELETE, and DROP.
    *
    * @param sql the input SQL string
    * @throws java.sql.SQLException when update failed
    */
   public void executeUpdate (String sql) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the update instruction
      stmt.executeUpdate (sql);

      // close the instruction
      stmt.close ();
   }//end executeUpdate

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and outputs the results to
    * standard out.
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQueryAndPrintResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and output them to standard out.
      boolean outputHeader = true;
      while (rs.next()){
		 if(outputHeader){
			for(int i = 1; i <= numCol; i++){
			System.out.print(rsmd.getColumnName(i) + "\t");
			}
			System.out.println();
			outputHeader = false;
		 }
         for (int i=1; i<=numCol; ++i)
            System.out.print (rs.getString (i) + "\t");
         System.out.println ();
         ++rowCount;
      }//end while
      stmt.close ();
      return rowCount;
   }//end executeQuery

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the results as
    * a list of records. Each record in turn is a list of attribute values
    *
    * @param query the input query string
    * @return the query result as a list of records
    * @throws java.sql.SQLException when failed to execute the query
    */
   public List<List<String>> executeQueryAndReturnResult (String query) throws SQLException {
      // creates a statement object
      Statement stmt = this._connection.createStatement ();

      // issues the query instruction
      ResultSet rs = stmt.executeQuery (query);

      /*
       ** obtains the metadata object for the returned result set.  The metadata
       ** contains row and column info.
       */
      ResultSetMetaData rsmd = rs.getMetaData ();
      int numCol = rsmd.getColumnCount ();
      int rowCount = 0;

      // iterates through the result set and saves the data returned by the query.
      boolean outputHeader = false;
      List<List<String>> result  = new ArrayList<List<String>>();
      while (rs.next()){
        List<String> record = new ArrayList<String>();
		for (int i=1; i<=numCol; ++i)
			record.add(rs.getString (i));
        result.add(record);
      }//end while
      stmt.close ();
      return result;
   }//end executeQueryAndReturnResult

   /**
    * Method to execute an input query SQL instruction (i.e. SELECT).  This
    * method issues the query to the DBMS and returns the number of results
    *
    * @param query the input query string
    * @return the number of rows returned
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int executeQuery (String query) throws SQLException {
       // creates a statement object
       Statement stmt = this._connection.createStatement ();

       // issues the query instruction
       ResultSet rs = stmt.executeQuery (query);

       int rowCount = 0;

       // iterates through the result set and count nuber of results.
       while (rs.next()){
          rowCount++;
       }//end while
       stmt.close ();
       return rowCount;
   }

   /**
    * Method to fetch the last value from sequence. This
    * method issues the query to the DBMS and returns the current
    * value of sequence used for autogenerated keys
    *
    * @param sequence name of the DB sequence
    * @return current value of a sequence
    * @throws java.sql.SQLException when failed to execute the query
    */
   public int getCurrSeqVal(String sequence) throws SQLException {
	Statement stmt = this._connection.createStatement ();

	ResultSet rs = stmt.executeQuery (String.format("Select currval('%s')", sequence));
	if (rs.next())
		return rs.getInt(1);
	return -1;
   }

   /**
    * Method to close the physical connection if it is open.
    */
   public void cleanup(){
      try{
         if (this._connection != null){
            this._connection.close ();
         }//end if
      }catch (SQLException e){
         // ignored.
      }//end try
   }//end cleanup

   /**
    * The main execution method
    *
    * @param args the command line arguments this inclues the <mysql|pgsql> <login file>
    */
   public static void main (String[] args) {
      if (args.length != 3) {
         System.err.println (
            "Usage: " +
            "java [-classpath <classpath>] " +
            Amazon.class.getName () +
            " <dbname> <port> <user>");
         return;
      }//end if

      Greeting();
      Amazon esql = null;
      try{
         // use postgres JDBC driver.
         Class.forName ("org.postgresql.Driver").newInstance ();
         // instantiate the Amazon object and creates a physical
         // connection.
         String dbname = args[0];
         String dbport = args[1];
         String user = args[2];
         esql = new Amazon (dbname, dbport, user, "");

         boolean keepon = true;
         while(keepon) {
            // These are sample SQL statements
            System.out.println("MAIN MENU");
            System.out.println("---------");
            System.out.println("1. Create user");
            System.out.println("2. Log in");
            System.out.println("9. < EXIT");
            String authorisedUser = null;
            switch (readChoice()){
               case 1: CreateUser(esql); break;
               case 2: authorisedUser = LogIn(esql); break;
               case 9: keepon = false; break;
               default : System.out.println("Unrecognized choice!"); break;
            }//end switch
            if (authorisedUser != null) {
              boolean usermenu = true;
              while(usermenu) {
		      switch (loggedInUserType) {
			      case "admin":
				      break;
			      case "manager":
				      System.out.println("MAIN MENU");
                		      System.out.println("---------");
				      System.out.println("1. View Stores within 30 miles");
				      System.out.println("2. View Product List");
				      System.out.println("3. Place a Order");
			              System.out.println("4. View 5 recent orders");

			   	      //the following functionalities basically used by managers
				      System.out.println("5. Update Product");
				      System.out.println("6. View 5 recent Product Updates Info");
			              System.out.println("7. View 5 Popular Items");
			              System.out.println("8. View 5 Popular Customers");
			              System.out.println("9. Place Product Supply Request to Warehouse");

				      System.out.println(".........................");
				      System.out.println("20. Log out");
				      switch (readChoice()){
					      case 1: viewStores(esql); break;
			   		      case 2: viewProducts(esql); break;
					      case 3: placeOrder(esql); break;
					      case 4: viewRecentOrders(esql); break;
					      case 5: updateProduct(esql); break;
					      case 6: viewRecentUpdates(esql); break;
					      case 7: viewPopularProducts(esql); break;
					      case 8: viewPopularCustomers(esql); break;
					      case 9: placeProductSupplyRequests(esql); break;

					      case 20: usermenu = false; loggedInUserID = -1; loggedInUserType = "customer"; break;
					      default : System.out.println("Unrecognized choice!"); break;
				      } 
			      case "customer":
				      System.out.println("MAIN MENU");
                		      System.out.println("---------");
				      System.out.println("1. View Stores within 30 miles");
				      System.out.println("2. View Product List");
				      System.out.println("3. Place a Order");
			              System.out.println("4. View 5 recent orders");

				      System.out.println(".........................");
				      System.out.println("20. Log out");
				      switch (readChoice()){
					      case 1: viewStores(esql); break;
			   		      case 2: viewProducts(esql); break;
					      case 3: placeOrder(esql); break;
					      case 4: viewRecentOrders(esql); break;

					      case 20: usermenu = false; loggedInUserID = -1; break;
					      default : System.out.println("Unrecognized choice!"); break;
				      }
		      }
              }
            }
         }//end while
      }catch(Exception e) {
         System.err.println (e.getMessage ());
      }finally{
         // make sure to cleanup the created table and close the connection.
         try{
            if(esql != null) {
               System.out.print("Disconnecting from database...");
               esql.cleanup ();
               System.out.println("Done\n\nBye !");
            }//end if
         }catch (Exception e) {
            // ignored.
         }//end try
      }//end try
   }//end main

   public static void Greeting(){
      System.out.println(
         "\n\n*******************************************************\n" +
         "              User Interface      	               \n" +
         "*******************************************************\n");
   }//end Greeting

   /*
    * Reads the users choice given from the keyboard
    * @int
    **/
   public static int readChoice() {
      int input;
      // returns only if a correct value is given.
      do {
         System.out.print("Please make your choice: ");
         try { // read the integer, parse it and break.
            input = Integer.parseInt(in.readLine());
            break;
         }catch (Exception e) {
            System.out.println("Your input is invalid!");
            continue;
         }//end try
      }while (true);
      return input;
   }//end readChoice

   /*
    * Creates a new user
    **/
   public static void CreateUser(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();
         System.out.print("\tEnter latitude: ");   
         String latitude = in.readLine();       //enter lat value between [0.0, 100.0]
         System.out.print("\tEnter longitude: ");  //enter long value between [0.0, 100.0]
         String longitude = in.readLine();
	 
	 // Check for duplicate names
         String query = String.format("SELECT name, password FROM Users WHERE name = '%s'", name);
	 List<List<String>> userData = esql.executeQueryAndReturnResult(query);
         if (!userData.isEmpty() && userData.get(0).get(1).equals(password)) {
		 System.err.println("Error: This user already exists.");
                 return;
         }
	 
	 // Check if password matches name
	 if (password.equals(name)) {
		 System.err.println("Error: Password should not be the same as the user name.");
		 return;
	 }

	 // Check for password strength
	 boolean hasUpperCase = !password.equals(password.toLowerCase());
	 boolean hasNumber = password.matches(".*\\d.*");
	 boolean hasSpecialChar = !password.matches("[A-Za-z0-9]");
	 if (password.length() < 5 || !hasUpperCase || !hasNumber || !hasSpecialChar) {
		 System.err.println("Error: Password must be between 5-11 characters and must have one capital letter, one number, and one special character.");
		 return;
	 }

	 // Check if latitude is within the valid range
	 double lat = Double.parseDouble(latitude);
	 if(lat <= 0.0 || lat >= 100.0) {
		 System.err.println("Error: Latitude must be between 0.0 and 100.0.");
	          return;
	 }
	 
	 // Check if longitude is within the valid range
	 double lon = Double.parseDouble(longitude);
	 if(lon <= 0.0 || lon >= 100.0) {
		 System.err.println("Error: Longitude must be between 0.0 and 100.0.");
	         return;
	 }

         String type="Customer";
	 
	 query = String.format("INSERT INTO Users (name, password, latitude, longitude, type) VALUES ('%s','%s', %s, %s,'%s')", name, password, latitude, longitude, type);

         esql.executeUpdate(query);
         System.out.println ("User successfully created!");
      }catch(Exception e){
	      System.err.println(e.getMessage());
      }
   }//end CreateUser


   /*
    * Check log in credentials for an existing user
    * @return User login or null is the user does not exist
    **/
   public static String LogIn(Amazon esql){
      try{
         System.out.print("\tEnter name: ");
         String name = in.readLine();
         System.out.print("\tEnter password: ");
         String password = in.readLine();

         String query = String.format("SELECT * FROM Users WHERE name = '%s' AND password = '%s'", name, password);
         List<List<String>> userRecords = esql.executeQueryAndReturnResult(query);
	 if (!userRecords.isEmpty()) {
		 loggedInUserID = Integer.parseInt(userRecords.get(0).get(0));
		 loggedInUserType = userRecords.get(0).get(5).trim();
		 return name;
	 }
	 System.err.println("Error: Login failed.");
         return null;
      }catch(Exception e){
         System.err.println (e.getMessage ());
         return null;
      }
   }//end

// Rest of the functions definition go in here

   /*
    * Displays the list of stores within a 3-mile radius from the user's location.
    * */
   public static void viewStores(Amazon esql) {
	   try {
		   // Get current user's location
		   String query = String.format("SELECT latitude, longitude FROM Users WHERE userID = '%s'", loggedInUserID);
		   List<List<String>> userData = esql.executeQueryAndReturnResult(query);
		   if (userData.isEmpty()) {
			   System.err.println("Error: User not found");
			   return;
		   }
		   double userLat = Double.parseDouble(userData.get(0).get(0));
		   double userLong = Double.parseDouble(userData.get(0).get(1));

		   // Retrieve all stores
		   query = "SELECT storeID, latitude, longitude FROM Store";
		   List<List<String>> storeData = esql.executeQueryAndReturnResult(query);

		   System.out.println("Stores within 30 miles:");
		   boolean found = false;
		   System.out.printf("%-10s %-10s %-10s\n", "Store ID", "Latitude", "Longitude");
		   for (List<String> store : storeData) {
			   int storeID = Integer.parseInt(store.get(0));
			   double storeLat = Double.parseDouble(store.get(1));
			   double storeLong = Double.parseDouble(store.get(2));

			   // Calculate distance
			   double distance = esql.calculateDistance(userLat, userLong, storeLat, storeLong);
			   if (distance <= 30.0) {
				   found =true;
				   System.out.printf("%-10d %-10.6f %10.6f \n", storeID, storeLat, storeLong);
			   }
		   }
		   if (!found) {
			   System.out.println("No stores found within 30 miles.");
		   }
	   } catch(Exception e) {
		   System.err.println(e.getMessage());
	   }
   }

   /*
    * View products available in a specific store. Validate the store ID input
    */
   public static void viewProducts(Amazon esql) {
	   try {
		   System.out.print("\tEnter Store ID: ");
		   String storeIdInput = in.readLine().trim();
		   if (storeIdInput.isEmpty() || !storeIdInput.matches("\\d+")) { // Checks if input is a number
			   System.err.println("Error: Invalid Store ID.");
			   return;
		   }
		   int storeID = Integer.parseInt(storeIdInput);

		   // Check if the store exists
		   String query = String.format("SELECT * FROM Store WHERE storeID = %d", storeID);
		   int storeExists = esql.executeQuery(query);
		   if (storeExists < 1) {
			   System.err.println("Error: Store does not exist.");
			   return;
		   }

		   query = String.format("SELECT productName, numberOfUnits, pricePerUnit FROM Product WHERE storeID = %d", storeID);
		   int productCount = esql.executeQueryAndPrintResult(query);
		   if (productCount == 0) {
			   System.out.println("No products found for this store.");
		   }
	   } catch(Exception e) {
		   System.err.println(e.getMessage());
	   }
   }

   /*
    * Allows users to place an order, checks for valid store ID, product name, and units.
    * Additionally, verifies if the store has the product in stock and sufficient quantity
    * before placing the order.
    */
   public static void placeOrder(Amazon esql) {
	   try {
		   System.out.print("\tEnter Store ID: ");
		   String storeIdInput = in.readLine().trim();
		   if (storeIdInput.isEmpty() || !storeIdInput.matches("\\d+")) {
			   System.err.println("Error: Invalid Store ID.");
			   return;
		   }
		   int storeID = Integer.parseInt(storeIdInput);

		   System.out.print("\tEnter Product Name: "); String productName = in.readLine().trim();
		   if (productName.isEmpty()) {
			   System.err.println("Error: Invalid Product Name.");
			   return;
		   }

		   System.out.print("\tEnter Number of Units: ");
		   String unitsInput = in.readLine().trim();
		   if (unitsInput.isEmpty() || !unitsInput.matches("\\d+") || Integer.parseInt(unitsInput) < 1) {
			   System.err.println("Error: Invalid Number of Units.");
			   return;
		   }
		   int units = Integer.parseInt(unitsInput);
		   
		   String query = String.format("SELECT * FROM Store WHERE storeID = %d", storeID);
		   int storeExists = esql.executeQuery(query);
		   if (storeExists < 1) {
			   System.err.println("Error: Store does not exist.");
			   return;
		   }

		   // Check if the store has the product
		   query = String.format("SELECT numberOfUnits FROM Product WHERE storeID = %d AND productName = '%s'", storeID, productName);
		   List<List<String>> productData = esql.executeQueryAndReturnResult(query);
		   if (productData.isEmpty()) {
			   System.err.println("Error: Product not found in the specified store.");
			   return;
		   }
		   // Check if the store has enough stock for the order
		   int availableUnits = Integer.parseInt(productData.get(0).get(0));
		   if (units > availableUnits) {
			   System.err.println("Error: Insufficient stock for the product.");
			   return;
		   }

		   // Place the order
		   query = String.format("INSERT INTO Orders (customerID, storeID, productName, unitsOrdered, orderTime) VALUES (%d, %d, '%s', %d, NOW())", loggedInUserID, storeID, productName, units);
		   esql.executeUpdate(query);
		   System.out.println ("Order successfully created!");
	   } catch(Exception e) {
		   System.err.println(e.getMessage());
	   }
   }


   public static void viewRecentOrders(Amazon esql) {}
   
   /* 
    * Update product information after validating store ID, product name, new units, and new price.
    */
   public static void updateProduct(Amazon esql) {
	   try {
		   System.out.print("\tEnter Store ID: ");
		   String storeIdInput = in.readLine().trim();
		   if (storeIdInput.isEmpty() || !storeIdInput.matches("\\d+")) {
			   System.err.println("Error: Invalid Store ID.");
			   return;
		   }
		   int storeID = Integer.parseInt(storeIdInput);

		   System.out.print("\tEnter Product Name: ");
		   String productName = in.readLine().trim();
		   if (productName.isEmpty()) {
			   System.err.println("Error: Invalid Product Name.");
			   return;
		   }

		   System.out.print("\tEnter New Number of Units (leave empty if no change): ");
		   String newUnitsInput = in.readLine().trim();
		   Integer newUnits = null;
		   if (!newUnitsInput.isEmpty()) {
			   if (!newUnitsInput.matches("\\d+") || Integer.parseInt(newUnitsInput) < 0) {
				   System.err.println("Error: Invalid Number of Units.");
				   return;
			   }
			   newUnits = Integer.parseInt(newUnitsInput);
		   }

  		   System.out.print("\tEnter New Price Per Unit (leave empty if no change): ");
		   String newPriceInput = in.readLine().trim();
		   Integer newPrice = null;
		   if (!newPriceInput.isEmpty()) {
			   if (!newPriceInput.matches("\\d+") || Integer.parseInt(newPriceInput) < 0) {
				   System.err.println("Error: Invalid Price Per Unit.");
				   return;
			   }
			   newPrice = Integer.parseInt(newPriceInput);
		   }

		   String query = String.format("SELECT * FROM Store WHERE storeID = %d", storeID);
		   int storeExists = esql.executeQuery(query);
		   if (storeExists < 1) {
			   System.err.println("Error: Store does not exist.");
			   return;
		   }

		   // Check if the user is the manager of the store
		   query = String.format("SELECT managerID FROM Store WHERE storeID = %d", storeID);
		   List<List<String>> managerResults = esql.executeQueryAndReturnResult(query);
		   if (managerResults.isEmpty() || Integer.parseInt(managerResults.get(0).get(0)) != loggedInUserID) {
			   System.err.println("Error: You are not the manager of this store.");
			   return;
		   }

		   // Check if the product exists in the store
		   query = String.format("SELECT * FROM Product WHERE storeID = %d AND productName = '%s'", storeID, productName);
		   int productExists = esql.executeQuery(query);
		   if (productExists < 1) {
			   System.err.println("Error: Product not found in the specified store.");
			   return;
		   }
		   
		   // Only update fields if new values have been provided
		   List<String> updateParts = new ArrayList<>();
		   if (newUnits != null) updateParts.add(String.format("numberOfUnits = %d", newUnits));
		   if (newPrice != null) updateParts.add(String.format("pricePerUnit = %d", newPrice));
		   if (updateParts.isEmpty()) {
			   System.out.println("No updates made to the product.");
			   return;
		   }

		   query += String.join(", ", updateParts) + String.format(" WHERE storeID = %d AND productName = '%s'", storeID, productName);
		   esql.executeUpdate(query);
		   System.out.println("Product information updated successfully!");
	   } catch (Exception e) {
		   System.err.println(e.getMessage());
	   }
   }

   public static void viewRecentUpdates(Amazon esql) {}
   public static void viewPopularProducts(Amazon esql) {}
   public static void viewPopularCustomers(Amazon esql) {}
   public static void placeProductSupplyRequests(Amazon esql) {}

}//end Amazon

