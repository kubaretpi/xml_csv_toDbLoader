package main.db;

import main.entity.Customer;
import main.validation.RecognizeTypeOfContact;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DBQuery {

    private static DBQuery dbQuery = null;

    private DBQuery() {

    }


    public void insertDataCustomers(List<Customer> customers) throws SQLException {
        insertData(customers);
    }


    private void insertData(List<Customer> customers) throws SQLException {

        String queryCustomer = "INSERT INTO customers (name,surname,age) VALUES (?,?,?)";
        String queryContacts = "INSERT INTO contacts (id_customer,type,contact) VALUES (?,?,?)";

        ResultSet resultSet = null;
        PreparedStatement pStatementCustomer = null;
        PreparedStatement pStatementContacts = null;
        Connection connection = null;
        RecognizeTypeOfContact recognizeTypeOfContact = RecognizeTypeOfContact.getInstance();

        try {

            connection = DBConnection.connect();

            if (connection != null) {
                pStatementCustomer = connection.prepareStatement(queryCustomer, Statement.RETURN_GENERATED_KEYS);
                pStatementContacts = connection.prepareStatement(queryContacts, Statement.RETURN_GENERATED_KEYS);

                for (Customer customer : customers) {

                    //dodanie klienta
                    pStatementCustomer.setString(1, customer.getName());
                    pStatementCustomer.setString(2, customer.getSurname());

                    if (customer.getAge() != null)
                        pStatementCustomer.setInt(3, customer.getAge());
                    else
                        pStatementCustomer.setNull(3, Types.INTEGER);

                    pStatementCustomer.executeUpdate();

                    resultSet = pStatementCustomer.getGeneratedKeys();
                    resultSet.next();
                    int autoGeneratedIdCustomer = resultSet.getInt(1);

                    //dodanie kontaktu
                    if (customer.getContact() != null) {
                        recognizeTypeOfContact.recognize(customer.getContact());

                        for (Map.Entry<String, String> entry : recognizeTypeOfContact.getPreparedContacts().entrySet()) {
                            pStatementContacts.setInt(1, autoGeneratedIdCustomer);
                            pStatementContacts.setString(2, entry.getValue());
                            pStatementContacts.setString(3, entry.getKey());
                            pStatementContacts.addBatch();
                        }

                        pStatementContacts.executeBatch();
                        pStatementContacts.clearBatch();

                    }
                }
                System.out.println("Loading complete");
            }

        } catch (
                SQLException e) {
            e.printStackTrace();
        } finally {
            closeSession(pStatementCustomer, pStatementContacts, connection, resultSet);
        }

    }

    private void closeSession(PreparedStatement pStatementCustomer, PreparedStatement pStatementContacts, Connection connection, ResultSet resultSet) throws SQLException {
        if (pStatementCustomer != null) {
            pStatementCustomer.close();
        }
        if (pStatementContacts != null) {
            pStatementContacts.close();
        }
        if (connection != null) {
            connection.close();
        }
        if (resultSet != null) {
            resultSet.close();
        }
    }


    public static DBQuery getInstance() {
        if (dbQuery == null) {
            dbQuery = new DBQuery();
        }
        return dbQuery;
    }


}


