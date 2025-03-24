package edu.upb.tresenraya.database;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ContactoDAO {

    private static final String INSERT_CONTACT = "INSERT INTO contactos (nombre, ip) VALUES (?, ?)";
    private static final String SELECT_CONTACTS = "SELECT nombre, ip FROM contactos";
    private static final String CHECK_DUPLICATE = "SELECT COUNT(*) FROM contactos WHERE nombre = ? AND ip = ?";

    // Agrega un contacto solo si no existe ya con ese nombre e IP
    public static void agregarContacto(String nombre, String ip) {
        if (existeContacto(nombre, ip)) {
            System.out.println("Contacto duplicado (no se insertó): " + nombre + " (" + ip + ")");
            return;
        }

        try (PreparedStatement pstmt = ConexionDb.getInstance().getConnection().prepareStatement(INSERT_CONTACT)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, ip);
            pstmt.executeUpdate();
            System.out.println("Contacto agregado: " + nombre + " (" + ip + ")");
        } catch (SQLException e) {
            System.err.println("Error al agregar contacto: " + e.getMessage());
        }
    }

    // Verifica si ya existe un contacto con ese nombre e IP
    public static boolean existeContacto(String nombre, String ip) {
        try (PreparedStatement pstmt = ConexionDb.getInstance().getConnection().prepareStatement(CHECK_DUPLICATE)) {
            pstmt.setString(1, nombre);
            pstmt.setString(2, ip);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error al verificar duplicado: " + e.getMessage());
        }
        return false;
    }

    // Obtiene todos los contactos de la base de datos
    public static List<String[]> obtenerContactos() {
        List<String[]> contactos = new ArrayList<>();
        try (Statement stmt = ConexionDb.getInstance().getConnection().createStatement(); ResultSet rs = stmt.executeQuery(SELECT_CONTACTS)) {

            while (rs.next()) {
                contactos.add(new String[]{rs.getString("nombre"), rs.getString("ip")});
            }
            System.out.println("Contactos cargados desde la base de datos.");
        } catch (SQLException e) {
            System.err.println("Error al obtener contactos: " + e.getMessage());
        }
        return contactos;
    }
}
