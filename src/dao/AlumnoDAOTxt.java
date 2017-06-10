package dao;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import persona.Alumno;
import persona.AlumnoInvalidoException;
import persona.FechaInvalidaException;
import persona.MiCalendar;
import persona.Persona;
import persona.PersonaInvalidaException;

/*
* Listado de campos
* 0. DNI
* 1. Apellido y nombre
* 2. Fecha de nacimiento
* 3. Sexo
* 4. Cantidad de materias aprobadas
* 5. Promedio
* 6. Fecha de ingreso
* 7. Estado del alumno
 */
public class AlumnoDAOTxt extends DAO<Alumno, Integer> {

    public AlumnoDAOTxt(File archivo) throws FileNotFoundException {
        raf = new RandomAccessFile(archivo, "rws");
    }

    /*
    * insertar(Alumno alu)
    *
    * @params objeto tipo Alumno
    * @throws DAOException
    * @returns void
     */
    @Override
    public void insertar(Alumno alu) throws DAOException {
        //Comprueba que el objeto no sea nulo
        if (alu != null) {

            if (existe(alu.getDni())) {
                throw new DAOException("El alumno con DNI " + alu.getDni() + " ya existe.");
            }

            //Al ser nuevo, el estado esta en activo por default
            String linea = alu.toString() + Persona.DELIM + "A" + System.lineSeparator();

            try {
                raf.seek(raf.length());
                raf.writeBytes(linea);
            } catch (IOException ex) {
                Logger.getLogger(AlumnoDAOTxt.class.getName()).log(Level.SEVERE, null, ex);
                throw new DAOException("No se pudo insertar: " + ex.getMessage());
            }
        }
    }

    /*
    * actualizar(Alumno alu)
    *
    * @params objeto tipo Alumno
    * @throws DAOException
    * @returns void
     */
    @Override
    public void actualizar(Alumno obj) throws DAOException {
        //Intenta posicionarse en el archivo

        try {
            raf.seek(0);
        } catch (IOException ex) {
            Logger.getLogger(AlumnoDAOTxt.class.getName()).log(Level.SEVERE, null, ex);
        }

        String linea;
        long lineaArchivo = 0;
        boolean alumnoEncontrado = false;
        try {
            //Mientras que no encuentre un alumno y no se termine el archivo, continua buscando.
            while ((linea = raf.readLine()) != null && !alumnoEncontrado) {
                String[] campos = linea.split(Persona.DELIM);
                if (obj.getDni() == Integer.valueOf(campos[0])) {
                    raf.seek(lineaArchivo);
                    //Obtengo el estado del objeto que viene desde el ABM
                    raf.writeBytes(obj.toString() + Persona.DELIM + obj.getEstado() + System.lineSeparator());//Escribe toda la linea.
                    //Seteo la flag para detener el ciclo
                    alumnoEncontrado = true;
                } else {
                    //Setea en que posicion esta
                    lineaArchivo = raf.getFilePointer();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(AlumnoDAOTxt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    * eliminar (Alumno obj)
    *
    * @params objeto tipo Alumno
    * @returns void
     */
    @Override
    public void eliminar(Alumno obj) {

        //Setea el archivo en la posicion inicial
        try {
            raf.seek(0);
        } catch (IOException ex) {
            Logger.getLogger(AlumnoDAOTxt.class.getName()).log(Level.SEVERE, null, ex);
        }

        String linea;
        boolean alumnoEncontrado = false;
        long lineaArchivo = 0;

        try {
            while ((linea = raf.readLine()) != null && !alumnoEncontrado) {
                String[] campos = linea.split(Persona.DELIM);
                if (obj.getDni() == Integer.valueOf(campos[0]) && !"B".equals(campos[7])) {
                    raf.seek(lineaArchivo);//Setea la posicion
                    raf.writeBytes(obj.toString() + Persona.DELIM + "B" + System.lineSeparator());//Escribe toda la linea.
                    alumnoEncontrado = true;
                } else {
                    //Setea en que posicion esta
                    lineaArchivo = raf.getFilePointer();
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(AlumnoDAOTxt.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /*
    * buscar (Integer dni)
    *
    * @params Integer dni
    * @throws DAOException
    * @returns objeto tipo Alumno
     */
    @Override
    public Alumno buscar(Integer dni) throws DAOException {
        Alumno obj = new Alumno();

        String linea;

        try {
            raf.seek(0);
            while ((linea = raf.readLine()) != null) {
                String[] campos = linea.split(Persona.DELIM);
                if (Objects.equals(Integer.valueOf(campos[0]), dni)) {
                    setearCampos(obj, campos);
                    return obj;
                }
            }
        } catch (IOException ex) {
            throw new DAOException("Error al leer el archivo.\n" + ex.getMessage());

        } catch (NumberFormatException | FechaInvalidaException | PersonaInvalidaException ex) {
            Logger.getLogger(AlumnoDAOTxt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    /*
    * existe(Integer id)
    *
    * @params Integer id
    * @throws DAOException
    * @returns boolean
     */
    @Override
    public boolean existe(Integer id) throws DAOException {
        try {
            raf.seek(0);

            String[] campos;
            String linea;
            while ((linea = raf.readLine()) != null) {
                campos = linea.split(Persona.DELIM);

                if (Integer.parseInt(campos[0]) == id) {
                    return true;
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(AlumnoDAOTxt.class.getName()).log(Level.SEVERE, null, ex);
            throw new DAOException(ex.getMessage());
        }
        return false;
    }

    /*
    * getTodos()
    *
    * @returns Array con todos los registros del Archivo
     */
    @Override
    public List<Alumno> getTodos() {
        String linea;
        List<Alumno> listaAlu = new ArrayList<>();//necesito el array
        try {
            raf.seek(0);
            while ((linea = raf.readLine()) != null) {
                String[] campos = linea.split(Persona.DELIM);

                Alumno alu = new Alumno();
                setearCampos(alu, campos);
                listaAlu.add(alu);

            }
        } catch (IOException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumberFormatException | FechaInvalidaException | PersonaInvalidaException ex) {
            Logger.getLogger(AlumnoDAOTxt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaAlu;
    }

    /*
    * getHabilitados()
    *
    * @returns Array con todos los registros <<habilitados>> del Archivo
     */
    public List<Alumno> getHabilitados() {
        String linea;
        List<Alumno> listaAlu = new ArrayList<>();//necesito el array
        try {
            raf.seek(0);
            while ((linea = raf.readLine()) != null) {
                String[] campos = linea.split(Persona.DELIM);

                if (!"B".equals(campos[7])) {
                    Alumno alu = new Alumno();
                    setearCampos(alu, campos);
                    listaAlu.add(alu);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumberFormatException | FechaInvalidaException | PersonaInvalidaException ex) {
            Logger.getLogger(AlumnoDAOTxt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaAlu;
    }

    /*
    * getDeshabilitados()
    *
    * @returns Array con todos los registros <<deshabilitados>> del Archivo
     */
    public List<Alumno> getDeshabilitados() {
        String linea;
        List<Alumno> listaAlu = new ArrayList<>();//necesito el array
        try {
            raf.seek(0);
            while ((linea = raf.readLine()) != null) {
                String[] campos = linea.split(Persona.DELIM);

                if (!"A".equals(campos[7])) {
                    Alumno alu = new Alumno();
                    setearCampos(alu, campos);
                    listaAlu.add(alu);
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(DAO.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NumberFormatException | FechaInvalidaException | PersonaInvalidaException ex) {
            Logger.getLogger(AlumnoDAOTxt.class.getName()).log(Level.SEVERE, null, ex);
        }
        return listaAlu;
    }

    /*
    * setearCampos(Alumno alu, String[] campos)
    *
    * @params Objeto tipo alumno, Vector con valores 
    * @throws AlumnoInvalidoException, FechaInvalidaException, PersonaInvalidaException, NumberFormatException
    * @returns void
     */
    private void setearCampos(Alumno obj, String[] campos) throws NumberFormatException, FechaInvalidaException, AlumnoInvalidoException, PersonaInvalidaException {
        obj.setDni(Integer.valueOf(campos[0]));
        obj.setApyn(campos[1]);
        String[] fechaNac = campos[2].split("/");
        obj.setFechaNac(new MiCalendar(Integer.valueOf(fechaNac[0]), Integer.valueOf(fechaNac[1]), Integer.valueOf(fechaNac[2])));
        obj.setSexo(campos[3].charAt(0));
        obj.setCantMatAprob(Integer.valueOf(campos[4]));
        obj.setPromedio(Double.valueOf(campos[5].replace(",", ".")));
        String[] fechaIngreso = campos[6].split("/");
        obj.setFechaIngr(new MiCalendar(Integer.valueOf(fechaIngreso[0]), Integer.valueOf(fechaIngreso[1]), Integer.valueOf(fechaIngreso[2])));
        obj.setEstado(campos[7].charAt(0));
    }
    private RandomAccessFile raf;
}
