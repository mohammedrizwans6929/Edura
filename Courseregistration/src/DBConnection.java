import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
	private static final String URL ="jdbc:mysql://localhost:3306/registrationdb?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    private static final String USER = "root";       // 👉 change this
    private static final String PASSWORD = "@Lionelmessi10"; // 👉 change this

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}


