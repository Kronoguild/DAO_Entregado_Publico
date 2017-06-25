package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import persona.Alumno;
import persona.MiCalendar;
import persona.PersonaInvalidaException;

public class AlumnoDAOBD extends DAO<Alumno, Integer> {

    public AlumnoDAOBD() throws SQLException, InstantiationException, IllegalAccessException {
        DriverManager.registerDriver(new com.mysql.jdbc.Driver ());
        try {
            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        conexion = DriverManager.getConnection("jdbc:mysql://localhost:330/alumnos?verifyServerCertificate=false&useSSL=true", "root", "root");

        //Insertar
        String sentencia
                = "INSERT INTO alumnos\n"
                + "(dni, apyn, sexo, fechaNac, promedio, cantMatAprob, fechaIngr)\n"
                + "VALUES\n"
                + "(?, ?, ?, ?, ?, ?, ?);\n";

        pStmtInsertar = conexion.prepareStatement(sentencia);

        //Buscar
        sentencia
                = "SELECT *\n"
                + "FROM alumnos\n"
                + "WHERE dni = ?\n";

        pStmtBuscar = conexion.prepareStatement(sentencia);

        //Eliminar (Baja fisica)
        sentencia
                = "DELETE FROM alumnos\n"
                + "WHERE dni = ?;\n";

        pStmtEliminar = conexion.prepareStatement(sentencia);

        //Actualizar
        sentencia
                = "UPDATE alumnos\n"
                + "SET apyn = ?,\n"
                + "sexo = ?,\n"
                + " fechaNac = ?,\n"
                + " promedio = ?,\n"
                + "cantMatAprob = ?,\n"
                + "fechaIngr = ?,\n"
                + "estado = ?\n"
                + "WHERE dni = ?";

        pStmtActualizar = conexion.prepareStatement(sentencia);
    }

    @Override
    public void insertar(Alumno alu) throws DAOException {
        Integer numeroDni = alu.getDni();

        //Antes de realizar el proceso, reviso si existe el alumnos con ese dni
        if (existe(numeroDni)) {
            throw new DAOException("El alumnos a insertar existe en la base de datos.");
        }

        try {
            pStmtInsertar.setInt(1, alu.getDni());
            pStmtInsertar.setString(2, alu.getApyn());
            pStmtInsertar.setString(3, String.valueOf(alu.getSexo()));
            pStmtInsertar.setDate(4, alu.getFechaNac().toDate());
            pStmtInsertar.setDouble(5, alu.getPromedio());
            pStmtInsertar.setInt(6, alu.getCantMatAprob());
            pStmtInsertar.setDate(7, alu.getFechaIngr().toDate());

            pStmtInsertar.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Error en insertar: " + ex.getMessage());
        }
    }

    @Override
    public void actualizar(Alumno obj) throws DAOException {
        Integer numeroDni = obj.getDni();

        //Antes de realizar el proceso, reviso si existe el alumnos con ese dni
        if (!existe(numeroDni)) {
            throw new DAOException("El alumnos a actualizar no existe en la base de datos.");
        }

        try {
            pStmtActualizar.setString(1, String.valueOf(obj.getApyn()));
            pStmtActualizar.setString(2, String.valueOf(obj.getSexo()));
            pStmtActualizar.setDate(3, obj.getFechaNac().toDate());
            pStmtActualizar.setDouble(4, obj.getPromedio());
            pStmtActualizar.setInt(5, obj.getCantMatAprob());
            pStmtActualizar.setDate(6, obj.getFechaIngr().toDate());
            pStmtActualizar.setString(7,String.valueOf(obj.getEstado()));
            pStmtActualizar.setInt(8, obj.getDni());

            pStmtActualizar.executeUpdate();
        } catch (SQLException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Error en actualizar: " + ex.getMessage());
        }
    }

    //Baja logica!
    @Override
    public void eliminar(Alumno obj) throws DAOException {
        Integer numeroDni = obj.getDni();

        //Antes de realizar el proceso, reviso si existe el alumnos con ese dni
        if (!existe(numeroDni)) {
            throw new DAOException("El alumnos a eliminar no existe en la base de datos.");
        }

        try {
            pStmtEliminar.setInt(1, numeroDni);
            pStmtEliminar.execute();
        } catch (SQLException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Eliminar falló: " + ex.getMessage());
        }
    }

    @Override
    public Alumno buscar(Integer id) throws DAOException {
        try {
            pStmtBuscar.setInt(1, id);

            ResultSet rs = pStmtBuscar.executeQuery();

            if (!rs.next()) {
                return null;
            }

            Alumno alu;
            try {
                alu = rsAAlumno(rs);
            } catch (PersonaInvalidaException ex) {
                Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
                throw new DAOException(ex.getMessage());
            }

            return alu;
        } catch (SQLException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Error en buscar: " + ex.getMessage());
        }

    }

    @Override
    public boolean existe(Integer id) throws DAOException {
        try {
            //Seteo el dni
            pStmtBuscar.setInt(1, id);

            //Ejecuto la query
            ResultSet rs = pStmtBuscar.executeQuery();

            //Si no hay siguiente, significa que no existe
            if (!rs.next()) {
                return false;
            }
        } catch (SQLException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Si llega hasta aca, existe
        return true;
    }

    @Override
    public List<Alumno> getTodos() throws DAOException {

        List<Alumno> listaAlu = new ArrayList<>();
        Alumno alu;
        Statement statement;
        ResultSet rs;
        String sentencia
                = "SELECT *\n"
                + "FROM alumnos;\n";

        try {
            statement = conexion.createStatement();
            rs = statement.executeQuery(sentencia);
            while (rs.next()) {

                alu = rsAAlumno(rs);
                listaAlu.add(alu);
            }
        } catch (SQLException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Obtener todos falló: " + ex.getMessage());
        } catch (PersonaInvalidaException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaAlu;
    }

    @Override
    public List<Alumno> getHabilitados() throws DAOException {
        List<Alumno> listaAlu = new ArrayList<>();
        Alumno alu;
        Statement statement;
        ResultSet rs;
        String sentencia
                = "SELECT *\n"
                + "FROM alumnos\n"
                + "WHERE estado='A';\n";

        try {
            statement = conexion.createStatement();
            rs = statement.executeQuery(sentencia);
            while (rs.next()) {

                alu = rsAAlumno(rs);
                listaAlu.add(alu);
            }
        } catch (SQLException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Obtener habilitados falló: " + ex.getMessage());
        } catch (PersonaInvalidaException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaAlu;
    }

    @Override
    public List<Alumno> getDeshabilitados() throws DAOException {
        List<Alumno> listaAlu = new ArrayList<>();
        Alumno alu;
        Statement statement;
        ResultSet rs;
        String sentencia
                = "SELECT *\n"
                + "FROM alumnos\n"
                + "WHERE estado='B';\n";

        try {
            statement = conexion.createStatement();
            rs = statement.executeQuery(sentencia);
            while (rs.next()) {

                alu = rsAAlumno(rs);
                listaAlu.add(alu);
            }
        } catch (SQLException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Obtener deshabilitados falló: " + ex.getMessage());
        } catch (PersonaInvalidaException ex) {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaAlu;
    }

    private Alumno rsAAlumno(ResultSet rs) throws SQLException, PersonaInvalidaException {
        Alumno alu = new Alumno();

        alu.setDni(rs.getInt("dni"));
        alu.setApyn(rs.getString("apyn"));
        alu.setSexo(rs.getString("sexo").charAt(0));
        alu.setFechaNac(new MiCalendar(rs.getDate("fechaNac")));
        alu.setPromedio(rs.getDouble("promedio"));
        alu.setCantMatAprob(rs.getInt("cantMatAprob"));
        alu.setFechaIngr(new MiCalendar(rs.getDate("fechaIngr")));
        alu.setEstado(rs.getString("estado").charAt(0));

        return alu;
    }

    private Connection conexion;
    private PreparedStatement pStmtInsertar;
    private PreparedStatement pStmtBuscar;
    private PreparedStatement pStmtEliminar;
    private PreparedStatement pStmtActualizar;

}
