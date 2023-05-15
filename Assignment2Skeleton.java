/* 
This is a Java skeleton code to help you out how to start this assignment.
Please keep in mind that this is NOT a compilable/runnable java file.
Please feel free to use this skeleton code.
Please give a closer look at the "To Do" parts of this file. You may get an idea of how to finish this assignment. 
*/

import java.util.*;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.PrintWriter;

class Assign2Skeleton {
   
   static class StockData {	   
	   // To Do: 
	   // Create this class which should contain the information  (date, open price, high price, low price, close price) for a particular ticker
      private String date;
      private double opening_price;
      private double high_price;
      private double low_price;
      private double closing_price;
      
      public StockData(String date, Double opening_prev, Double Hx, Double Lx, Double closing_prev) {
         this.date = date;
         this.opening_price = opening_prev;
         this.high_price = Hx;
         this.low_price = Lx;
         this.closing_price = closing_prev;
      }
      
      public String getDate() {
         return date;
      }
      
      public double getOpening() {
         return opening_price;
      }
      
      public double getClosing() {
         return closing_price;
      }
      
      public void display() {
         System.out.printf("Date: %5s Open: %f High: %f Low: %f Close: %f%n", date, opening_price, high_price, low_price, closing_price);
      }
      
      public void display_avg(double avg) {
         System.out.printf("Date: %5s Open: %f High: %f Low: %f Close: %f (Average: %f)%n", date, opening_price, high_price, low_price, closing_price, avg);
      }
      
      public void adjusted(String date, Double opening_prev, Double Hx, Double Lx, Double closing_prev, int adjusted) {
         this.date = date;
         this.opening_price = opening_prev / adjusted;
         this.high_price = Hx / adjusted;
         this.low_price = Lx / adjusted;
         this.closing_price = closing_prev / adjusted;
      }
   }
   
   static Connection conn;
   static final String prompt = "Enter ticker symbol [start/end dates]: ";
   
   public static void main(String[] args) throws Exception {
      String paramsFile = "readerparams.txt";
      if (args.length >= 1) {
         paramsFile = args[0];
      }
      
      Properties connectprops = new Properties();
      connectprops.load(new FileInputStream(paramsFile));
      try {
         Class.forName("com.mysql.jdbc.Driver");
         String dburl = connectprops.getProperty("dburl");
         String username = connectprops.getProperty("user");
         conn = DriverManager.getConnection(dburl, connectprops);
         System.out.printf("Database connection %s %s established.%n", dburl, username);
         
         Scanner in = new Scanner(System.in);
         System.out.print(prompt);
         String input = in.nextLine().trim();
         
         while (input.length() > 0) {
            String[] params = input.split("\\s+");
            String ticker = params[0];
            String startdate = null, enddate = null;
            if (getName(ticker)) {
               if (params.length >= 3) {
                  startdate = params[1];
                  enddate = params[2];
               }               
               Deque<StockData> data = getStockData(ticker, startdate, enddate);
               System.out.println();
               System.out.println("Executing investment strategy");
               doStrategy(ticker, data);
            } 
            
            System.out.println();
            System.out.print(prompt);
            input = in.nextLine().trim();
         }

         // Close the database connection
         System.out.println("Database connection closed.");
         conn.close();

      } catch (SQLException ex) {
         System.out.printf("SQLException: %s%nSQLState: %s%nVendorError: %s%n",
                           ex.getMessage(), ex.getSQLState(), ex.getErrorCode());
      }
   }
   
   static boolean getName(String ticker) throws SQLException {
	  // To Do: 
	  // Execute the first query and print the company name of the ticker user provided (e.g., INTC to Intel Corp.) 
	  // Please don't forget to use a prepared statement
     
      PreparedStatement pstmt = conn.prepareStatement("select Name from company where Ticker = ?");
      pstmt.setString(1,ticker);
      ResultSet rs = pstmt.executeQuery();
      if(rs.next()) {
         System.out.printf("%s%n", rs.getString(1));
         pstmt.close();
         return true;
      } 
      else {
         System.out.printf("%s not found in database.%n", ticker);
         pstmt.close();
         return false;
      }
   }

   static Deque<StockData> getStockData(String ticker, String start, String end) throws SQLException {	  
	  // To Do: 
	  // Execute the second query which will return stock information of the ticker (descending on the transaction date)
	  // Please don't forget to use prepared statement	 
      
     //Use if/else statement for the prepared statement
      String query = "select * from pricevolume where Ticker = ?";
      PreparedStatement pstmt;
      
      if(start != null && end != null) {
         query += " and TransDate BETWEEN ? and ? Order by TransDate Desc;";
         pstmt = conn.prepareStatement(query);
                                                         
         pstmt.setString(1, ticker);
         pstmt.setString(2, start);
         pstmt.setString(3, end);
      }
      else {
         query += " Order by TransDate Desc;";
         pstmt = conn.prepareStatement(query);
         
         pstmt.setString(1, ticker);
      }
      ResultSet rs = pstmt.executeQuery();
      
      //Setting all the needed variables:
      Deque<StockData> result = new ArrayDeque<>();
      int count_Days = 0;
      int count_Splits = 0;
      double check = 1.0;
      int adjusted = 1;
      String date;
      double opening_prev;
      double Hx;
      double Lx;
      double closing_prev;
      double opening_next = 0.0;

	  // To Do: 
	  // Loop through all the dates of that company (descending order)
			// Find split if there is any (2:1, 3:1, 3:2) and adjust the split accordingly
			// Include the adjusted data to result (which is a Deque); You can use addFirst method for that purpose
      while(rs.next()) {
         //Save information from the day and time and use that to find the split. Similar to Assignment 1.
         //NOTE: Got to use Double.parseDouble() because where getting a Double from the database it return 
         //"    value" the spaces cannot convert to a double to it fail.
         count_Days += 1;
         date = rs.getString(2);
         opening_prev = Double.parseDouble(rs.getString(3).trim());
         Hx = Double.parseDouble(rs.getString(4).trim());
         Lx = Double.parseDouble(rs.getString(5).trim());
         closing_prev = Double.parseDouble(rs.getString(6).trim());
         
         StockData temp = new StockData(date, opening_prev, Hx, Lx, closing_prev);
         
         check = checkSplitStock(date, closing_prev, opening_next);
         adjusted *= check;
         if(check != 1) {
            count_Splits += 1;
         }
         temp.adjusted(date, opening_prev, Hx, Lx, closing_prev, adjusted);
         result.addFirst(temp);
         //temp.display(); // <- Check result of splitting
         
         opening_next = opening_prev;
      }
      
      pstmt.close();
      System.out.printf("%d splits in %d trading days%n", count_Splits, count_Days);
	         
      return result;
   }
   
   static double checkSplitStock(String date, Double closing_prev, Double opening_next) {
      if(Math.abs(closing_prev / opening_next - 2.0) < .20) {
         System.out.println(String.format("%-25s %6.2f %-5s %,.2f", "2:1 split on " + date, closing_prev, "--->", opening_next));
         return 2;
      }
      else if(Math.abs(closing_prev / opening_next - 3.0) < .30) {
         System.out.println(String.format("%-25s %6.2f %-5s %,.2f", "3:1 split on " + date, closing_prev, "--->", opening_next));
         return 3;
      }
      else if(Math.abs(closing_prev / opening_next - 1.5) < .15) {
         System.out.println(String.format("%-25s %6.2f %-5s %,.2f", "3:2 split on " + date, closing_prev, "--->", opening_next));
         return 1.5;
      }
      return 1;
   }
   
   static void doStrategy(String ticker, Deque<StockData> data) {
	  //To Do: 
	  // Apply Steps 2.6 to 2.10 explained in the assignment description 
	  // data (which is a Deque) has all the information (after the split adjustment) you need to apply these steps
     //Setting All Variables
      List<Double> avg_lst = new ArrayList<>();
      double average;
      int n = data.size();
      double cash = 0.0;
      int shares = 0;
      int count_Transactions = 0;
      
      if(n < 50) {
         System.out.println("Net Gain of Zero");
      }
      else {
         for(int i = 0; i < n -1; i++) {
            if(i < 50) {
               avg_lst.add(data.getFirst().getClosing());
               data.getFirst().display(); // <- DEBUGGING Print Information
               if(i == 49) {
                  average = findAverage(avg_lst);
                  System.out.printf("Day Fifty: %s, average = %f%n", data.getFirst().getDate(), average); // <- DEBUGGING Print Information
               }
               data.removeFirst();
            }
            else if(i >= 50){
               average = findAverage(avg_lst);
             data.getFirst().display_avg(average); // <- DEBUGGING Print Information
             //Add code here for 2.9
               if(data.getFirst().getClosing() < average && data.getFirst().getClosing() / data.getFirst().getOpening() < 0.97000001) {
                  avg_lst.remove(0);
                  avg_lst.add(data.getFirst().getClosing());
                  data.removeFirst();
                  
                  cash -= (data.getFirst().getOpening() * 100);
                  cash -= 8;
                  shares += 100;
                  System.out.printf("Buy: %s 100 shares @ %f, total shares = %d, cash = %f%n", data.getFirst().getDate(), data.getFirst().getOpening(), shares, cash); // <- DEBUGGING Print Information
                  count_Transactions += 1;
               }
               else if(shares >= 100 && data.getFirst().getOpening() > average && data.getFirst().getOpening() / avg_lst.get(avg_lst.size() -1) > 1.00999999) { 
                  avg_lst.remove(0);
                  avg_lst.add(data.getFirst().getClosing());
                  String date_Sell = data.getFirst().getDate();
                  double cost_of_Share = (data.getFirst().getOpening() + data.getFirst().getClosing()) / 2;
                  data.removeFirst();
                  cash += (cost_of_Share * 100);
                  cash -= 8;
                  shares -= 100;
                  System.out.printf("Sell: %s 100 shares @ %f, total shares = %d, cash = %f%n", date_Sell, cost_of_Share, shares, cash); // <- DEBUGGING Print Information
                  count_Transactions += 1;
               }
               else {
                  avg_lst.remove(0);
                  avg_lst.add(data.getFirst().getClosing());
                  data.removeFirst();
               }
            }
         }
      }
      double opening = data.getFirst().getOpening();
      if(shares > 0) {
         cash += opening * shares;
      }
      System.out.printf("Transactions executed: %d%n", count_Transactions);
      System.out.printf("Net cash: %.2f%n", cash);
      System.out.printf("Final sale: %s 0 shares @ %f, cash = %f (average = %f)%n", data.getFirst().getDate(), opening, cash, findAverage(avg_lst)); // <- DEBUGGING Print Information
   }
   
   static double findAverage(List<Double> avg_lst) {
      double sum = 0;
               
      for(int j = 0; j < avg_lst.size(); j++) {
         sum += avg_lst.get(j);
      }
      return sum / avg_lst.size();
   }
}
