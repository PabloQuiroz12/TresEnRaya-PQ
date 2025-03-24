package edu.upb.tresenraya.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionDb {

    private static final String DB_URL = "jdbc:sqlite:databases/tresenraya.db";
    private static ConexionDb instancia;
    private Connection connection;

    private ConexionDb() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            System.out.println("Conexión a la base de datos establecida.");
            createTable(); // Crear tabla si no existe
        } catch (SQLException e) {
            System.err.println("Error al conectar a la base de datos: " + e.getMessage());
        }
    }

    private void createTable() throws SQLException {
        String sql = "CREATE TABLE IF NOT EXISTS contactos ("
                + " id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + " nombre TEXT NOT NULL,"
                + " ip TEXT NOT NULL,"
                + " UNIQUE(nombre, ip)"
                + ");";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
            System.out.println("Tabla 'contactos' verificada (con restricción de duplicados).");
        }
    }

    public static ConexionDb getInstance() {
        if (instancia == null) {
            instancia = new ConexionDb();
        }
        return instancia;
    }

    public Connection getConnection() {
        return connection;
    }

    public void cerrarConexion() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Conexión a la base de datos cerrada correctamente.");
            } catch (SQLException e) {
                System.err.println("Error al cerrar la base de datos: " + e.getMessage());
            }
        }
    }
}
