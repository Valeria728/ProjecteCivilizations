package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

// Clase singleton para gestionar la conexión a la base de datos.
//Solo crea una conexión y la reutiliza siempre.

public class DatabaseConnection {

    private static Connection connection = null;

    // Cambia estos valores según tu entorno
    private static final String URL      = "jdbc:mysql://127.0.0.1:3306/civilizations_db";
    private static final String USER     = "root";
    private static final String PASSWORD = "1234.";

    // Constructor privado: nadie puede crear instancias desde fuera
    private DatabaseConnection() {}

    public static Connection getConnection() {
        if (connection == null) {
            try {
                Class.forName("com.mysql.cj.jdbc.Driver");
                connection = DriverManager.getConnection(URL, USER, PASSWORD);
                System.out.println("Conexión a base de datos establecida.");
            } catch (ClassNotFoundException e) {
                System.out.println("Driver MySQL no encontrado: " + e.getMessage());
            } catch (SQLException e) {
                System.out.println("Error al conectar con la base de datos: " + e.getMessage());
            }
        }
        return connection;
    }

    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Conexión cerrada.");
            } catch (SQLException e) {
                System.out.println("Error al cerrar conexión: " + e.getMessage());
            }
        }
    }
}
