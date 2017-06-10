package dao;

import java.util.List;

public abstract class DAO<T, U>
{
    public abstract void insertar(T obj) throws DAOException;
    public abstract void actualizar(T obj) throws DAOException;
    public abstract void eliminar(T obj) throws DAOException;
    public abstract T buscar(U id) throws DAOException;
    public abstract boolean existe(U id) throws DAOException;
    public abstract List<T> getTodos() throws DAOException;
}
