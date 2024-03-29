package ua.kiev.prog.case2;

import ua.kiev.prog.shared.Id;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public abstract class AbstractDAO<K, T> {
    private final Connection conn;
    private final String table;

    public AbstractDAO(Connection conn, String table) {
        this.conn = conn;
        this.table = table;
    }

    public void add(T t) {
        try {
            Field[] fields = t.getClass().getDeclaredFields();

            StringBuilder names = new StringBuilder();
            StringBuilder values = new StringBuilder();

            for (Field f : fields) {
                f.setAccessible(true);

                names.append(f.getName()).append(',');
                values.append('"').append(f.get(t)).append("\",");
            }
            names.deleteCharAt(names.length() - 1); // last ','
            values.deleteCharAt(values.length() - 1); // last ','

            String sql = "INSERT INTO " + table + "(" + names.toString() +
                    ") VALUES(" + values.toString() + ")";

            try (Statement st = conn.createStatement()) {
                st.execute(sql.toString());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void delete(T t) {
        try {
            Field[] fields = t.getClass().getDeclaredFields();
            Field id = null;

            for (Field f : fields) {
                if (f.isAnnotationPresent(Id.class)) {
                    id = f;
                    id.setAccessible(true);
                    break;
                }
            }
            if (id == null)
                throw new RuntimeException("No Id field");

            String sql = "DELETE FROM " + table + " WHERE " + id.getName() +
                    " = \"" + id.get(t) + "\"";

            try (Statement st = conn.createStatement()) {
                st.execute(sql.toString());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void update(T t) {
        try {
            Field[] fields = t.getClass().getDeclaredFields();
            Field id = null;
            for (Field f : fields) {
                if (f.isAnnotationPresent(Id.class)) {
                    id = f;
                    id.setAccessible(true);
                    break;
                }
            }
            if (id == null)
                throw new RuntimeException("No Id field");

            StringBuilder sets = new StringBuilder();
            for (Field f : fields) {
                if (!f.getName().equals(id.getName())) {
                    f.setAccessible(true);
                    sets.append(f.getName()).append("=").append('"').append(f.get(t)).append("\",");
                }
            }
            sets.deleteCharAt(sets.length() - 1); // last ','
            String sql = "UPDATE " + table + " SET " + sets + " WHERE " + id.getName() +
                    " = \"" + id.get(t) + "\"";
            try (Statement st = conn.createStatement()) {
                st.execute(sql.toString());
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }

    }

    public List<T> getAll(Class<T> cls) {
        List<T> res = new ArrayList<>();

        try {
            try (Statement st = conn.createStatement()) {
                try (ResultSet rs = st.executeQuery("SELECT * FROM " + table)) {
                    ResultSetMetaData md = rs.getMetaData();

                    while (rs.next()) {
                        T client = (T) cls.newInstance();

                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            String columnName = md.getColumnName(i);

                            Field field = cls.getDeclaredField(columnName);
                            field.setAccessible(true);

                            field.set(client, rs.getObject(columnName));
                        }

                        res.add(client);
                    }
                }
            }

            return res;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public List<Object[]> getAll(Class<T> cls, String... fields) {
        List<Object[]> res = new ArrayList<>();

        try {
            try (Statement st = conn.createStatement()) {
                StringBuilder fielsNames = new StringBuilder();

                for (int i = 0; i < fields.length; i++) {
                    if (cls.getDeclaredField(fields[i])==null) {
                        throw new IllegalArgumentException("Wrong field name");
                    }
                    fielsNames.append(fields[i]).append(",");
                }
                fielsNames.deleteCharAt(fielsNames.length() - 1); // last ','
                try (ResultSet rs = st.executeQuery("SELECT " +fielsNames+" FROM " + table)) {
                    ResultSetMetaData md = rs.getMetaData();
                    Object[] client = new Object[md.getColumnCount()];
                    client=fields;
                    res.add(client);
                    while (rs.next()) {
                        //T client = (T) cls.newInstance();
                        client = new Object[md.getColumnCount()];
                        for (int i = 1; i <= md.getColumnCount(); i++) {
                            String columnName = md.getColumnName(i);
                            client[i-1]=rs.getString(columnName);
//                            Field field = cls.getDeclaredField(columnName);
//                            field.setAccessible(true);
//                            field.set(client, rs.getObject(columnName));
                        }
                        res.add(client);
                    }
                }
            }

            return res;
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public void listOut(List<Object[]> list){
        for (Object[] objects : list) {
            for (Object o : objects) {
                System.out.print(o+"\t\t");
            }
            System.out.println();
        }
    }


}
