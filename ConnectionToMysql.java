import java.sql.*;

public class ConnectionToMysql {
    static final String JDBC_DRIVER = "com.mysql.cj.jdbc.Driver";
    static final String DB_URL = "jdbc:mysql://localhost:3306/person?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
    static final String USER = "root";
    static final String PASS = "";

    public Connection con_to_mysql(){
        Connection con = null;
        Statement statement = null;

        try{
            Class.forName(JDBC_DRIVER);
            System.out.println("连接数据库................");
            con = DriverManager.getConnection(DB_URL,USER,PASS);
        }catch (ClassNotFoundException | SQLException e){
            e.printStackTrace();
        }
        return con;
    }

    public Statement getStatement(Connection connection) throws SQLException {
        System.out.println("实例化statment对象..................");
        return connection.createStatement();
    }

    public void select(Statement statement,String sql) throws SQLException {
        ResultSet resultSet = statement.executeQuery(sql);
        while (resultSet.next()) {
            System.out.printf("id:%d,name:%s,age:%d,gender:%s", resultSet.getInt("id"),
                    resultSet.getString("name"), resultSet.getInt("age"), resultSet.getString("gender"));
            System.out.println();
        }
    }
}
