package ua.kiev.prog.case2;

import ua.kiev.prog.shared.Client;
import ua.kiev.prog.shared.ConnectionFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Scanner;

public class Main {
    static final String DB_CONNECTION = "jdbc:mysql://localhost:3306/mydb?serverTimezone=UTC";
    static final String DB_USER = "root";
    static final String DB_PASSWORD = "admin77";

    public static void main(String[] args) throws SQLException {
        Scanner sc = new Scanner(System.in);

        ConnectionFactory factory = new ConnectionFactory(
                DB_CONNECTION, DB_USER, DB_PASSWORD
        );

        Connection conn = factory.getConnection();
        try {
            ClientDAOEx dao = new ClientDAOEx(conn, "clients");

            Client c = new Client("test", 1);
            dao.add(c);

            List<Client> list = dao.getAll(Client.class);
            for (Client cli : list)
                System.out.println(cli);

            c = list.get(0);
            c.setAge(44);
            dao.update(c);
            dao.listOut(dao.getAll(Client.class, "name", "age"));
        } finally {
            sc.close();
            if (conn != null) conn.close();
        }
    }
}
