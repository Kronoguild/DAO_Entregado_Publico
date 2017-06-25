package dao;

import java.util.List;
import persona.Alumno;

public abstract class DAO<T, U>
{
    public abstract void insertar(T obj) throws DAOException;
    public abstract void actualizar(T obj) throws DAOException;
    public abstract void eliminar(T obj) throws DAOException;
    public abstract T buscar(U id) throws DAOException;
    public abstract boolean existe(U id) throws DAOException;
    public abstract List<T> getTodos() throws DAOException;
    public abstract List<T> getHabilitados() throws DAOException;
    public abstract List<T> getDeshabilitados() throws DAOException;
}