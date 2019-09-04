package ua.kiev.prog.case2;

import ua.kiev.prog.shared.Client;

import java.sql.Connection;

/**
 * Created by Bios on 29.11.2017.
 */
public class ClientDAO2 extends AbstractDAO<Integer, Client> {
    public ClientDAO2(Connection conn, String table) {
        super(conn, table);
    }
}
