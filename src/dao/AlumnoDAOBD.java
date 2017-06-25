/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dao;
 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import persona.Alumno;
import persona.MiCalendar;
import persona.PersonaInvalidaException;
 
/**
 *
 * @author nestor
 */
public class AlumnoDAOBD extends DAO<Alumno, Integer>
{
    public AlumnoDAOBD() throws SQLException
    {
        conexion = DriverManager.getConnection("jdbc:mysql://localhost:330/alumnos", "root", "root");
         
        String sentencia =
            "insert into alumno\n" +
            "(dni, apyn, sexo, fechaNac, promedio, cantMatAprob, fechaIngr)\n" +
            "values\n" +
            "(?, ?, ?, ?, ?, ?, ?);\n";
         
        pStmtInsertar = conexion.prepareStatement(sentencia);
         
        sentencia =
            "select dni, apyn, sexo, fechaNac, promedio, cantMatAprob, fechaIngr, estado\n" +
            "from alumno\n" +
            "where dni = ?\n";
         
        pStmtBuscar = conexion.prepareStatement(sentencia);
    }
     
     
     
    @Override
    public void insertar(Alumno alu) throws DAOException
    {
        try
        {
            pStmtInsertar.setInt(1, alu.getDni());
            pStmtInsertar.setString(2, alu.getApyn());
            pStmtInsertar.setString(3, String.valueOf(alu.getSexo()));
           pStmtInsertar.setDate(4, alu.getFechaNac().toDate());
            pStmtInsertar.setDouble(5, alu.getPromedio());
            pStmtInsertar.setInt(6, alu.getCantMatAprob());
            pStmtInsertar.setDate(7, alu.getFechaIngr().toDate());
             
            pStmtInsertar.executeUpdate();
        }
        catch(SQLException ex)
        {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Error en insertar: " + ex.getMessage());
        }
    }
 
    @Override
    public void actualizar(Alumno obj) throws DAOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 
    @Override
    public void eliminar(Alumno obj) throws DAOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 
    @Override
    public Alumno buscar(Integer id) throws DAOException
    {
        try
        {
            pStmtBuscar.setInt(1, id);
             
            ResultSet rs = pStmtBuscar.executeQuery();
             
            if(!rs.next())
                return null;
             
            Alumno alu;
            try
            {
                alu = rsAAlumno(rs);
            }
            catch(PersonaInvalidaException ex)
            {
                Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
                throw new DAOException(ex.getMessage());
            }
             
            return alu;
        }
        catch(SQLException ex)
        {
            Logger.getLogger(AlumnoDAOBD.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException("Error en buscar: " + ex.getMessage());
        }
         
    }
 
    @Override
    public boolean existe(Integer id) throws DAOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
 
    @Override
    public List<Alumno> getTodos() throws DAOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public List<Alumno> getHabilitados() throws DAOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public List<Alumno> getDeshabilitados() throws DAOException
    {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
     
    private Connection conexion;
    private PreparedStatement pStmtInsertar;
    private PreparedStatement pStmtBuscar;
 
    private Alumno rsAAlumno(ResultSet rs) throws SQLException, PersonaInvalidaException
    {
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
}