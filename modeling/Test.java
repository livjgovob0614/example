import java.sql.Statement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;



public class Test {

  public static void main(String[] args) throws SQLException {
      Connection conn = DriverManager.getConnection("a@a", "a", "a");
      Statement stmt = conn.createStatement();

      StringBuffer sb = new StringBuffer();
      sb.append("select * from table1 ");

      ResultSet rs = stmt.executeQuery(sb.toString());
      while (rs.next()) {
        int a = rs.getInt(0);
        System.out.println("a: " + a);
      }
  }
}

