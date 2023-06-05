package com.example.imc.Controllers;

import com.example.imc.Handlers.DatabaseHandler;
import com.example.imc.Handlers.QueryHandler;
import com.example.imc.Models.Order;
import javafx.animation.FadeTransition;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.effect.BoxBlur;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Calendar;
import java.util.Date;

public class OrderController {
    QueryHandler queryHandler = new QueryHandler();
    Statement stmt;
    @FXML
    Text onErrorText;
    @FXML
    TableView<Order> tableView;
    @FXML
    TableColumn<Order, String> c1;
    @FXML
    TableColumn<Order, String> c2;
    @FXML
    TableColumn<Order, String> c3;
    @FXML
    TableColumn<Order, String> c4;
    @FXML
    TextField orderIDController;
    @FXML
    TextField productIDController;
    @FXML
    TextField quantityController;

    @FXML
    private Pane mainPane;

    @FXML
    private Pane popupPane;

    // For the add product button in the inventory view

    @FXML
    public void initialize() throws SQLException {
        stmt = DatabaseHandler.getStatement();

        ResultSet rs = stmt.executeQuery("select * from orders");

        while (rs.next()) {
            String orderID = rs.getString("OrderID");
            String productID = rs.getString("ProductID");
            String quantity = rs.getString("OrderQuantity");
            LocalDateTime dateTime = rs.getTimestamp("OrderDate").toLocalDateTime();
            Platform.runLater(() -> addOrder(orderID, productID, quantity, dateTime.toString()));
        }
        // Add event listener for delete key press
        tableView.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE || event.getCode() == KeyCode.DELETE) {
                // Get the selected product
                Order selectedOrder = tableView.getSelectionModel().getSelectedItem();
                if (selectedOrder != null) {
                    // Call a method to delete the row from the database
                    String orderID = selectedOrder.getOrderID();

                    boolean status = deleteFromDatabase(orderID);

                    if (status)
                    {
                        tableView.getItems().remove(selectedOrder);

                    }
                    else
                    {
                        String errorMessage = "Error deleting the order from the database";
                        Alert alert = new Alert(Alert.AlertType.ERROR);
                        alert.setTitle("Error");
                        alert.setHeaderText(null);
                        alert.setContentText(errorMessage);
                        alert.showAndWait();
                    }

                    // Remove the product from the TableView
                }
            }
        });
    }

    @FXML
    void kAddOrder() {
        // Apply a BoxBlur effect to the mainPane to make it blur
        BoxBlur blur = new BoxBlur(5, 5, 3);
        mainPane.setEffect(blur);

        // Make the popup pane visible and animate its fade-in
        popupPane.setOpacity(0);
        popupPane.setVisible(true);
        FadeTransition fadeInTransition = new FadeTransition(Duration.millis(300), popupPane);
        fadeInTransition.setToValue(1);
        fadeInTransition.play();
    }

    @FXML
    private void onConfirmClicked() {
        String orderID = orderIDController.getText();
        String productID = productIDController.getText();
        String quantity = quantityController.getText();
        LocalDateTime dateTime = LocalDateTime.now();

        // Create the Order object
        Order order = new Order(orderID, dateTime.toString(), productID, quantity);
        Date currentDate = Calendar.getInstance().getTime();
        boolean status = queryHandler.insertOrder(order.getOrderID(), order.getOrderDate(currentDate), order.getProductID(), order.getOrderQuantity());
        // Add the order to the UI
        if (status)
        {
            onErrorText.setVisible(false);
            addOrder(order.getOrderID(), order.getProductID(), order.getOrderQuantity(), order.getOrderDate(currentDate));
        }

        else {
            onErrorText.setVisible(true);
            return;
        }
        // Animate the popup pane's fade-out and then hide it
        FadeTransition fadeOutTransition = new FadeTransition(Duration.millis(300), popupPane);
        fadeOutTransition.setToValue(0);
        fadeOutTransition.setOnFinished(event -> popupPane.setVisible(false));
        fadeOutTransition.play();

        // Remove the BoxBlur effect from the mainPane
        mainPane.setEffect(null);
    }


    @FXML
    private void onDiscardClicked() {
        onErrorText.setVisible(false);
        // Animate the popup pane's fade-out and then hide it
        FadeTransition fadeOutTransition = new FadeTransition(Duration.millis(300), popupPane);
        fadeOutTransition.setToValue(0);
        fadeOutTransition.setOnFinished(event -> popupPane.setVisible(false));
        fadeOutTransition.play();

        // Remove the BoxBlur effect from the mainPane
        mainPane.setEffect(null);
    }

    public void addOrder(String orderID, String productID, String quantity, String dateTime) {
        // Create the custom row
        Order order = new Order(orderID, dateTime, productID, quantity);
        c1.setCellValueFactory(cellData -> cellData.getValue().orderIDProperty());
        c2.setCellValueFactory(cellData -> cellData.getValue().productIDProperty());
        c3.setCellValueFactory(cellData -> cellData.getValue().orderQuantityProperty());
        c4.setCellValueFactory(cellData -> {
            Date currentDate = Calendar.getInstance().getTime();

            String date = cellData.getValue().getOrderDate(currentDate);
            return new SimpleStringProperty(date);
        });
        // Add the custom row to the table
        tableView.getItems().add(order);

    }

    private boolean deleteFromDatabase(String id) {
        try {
            String deleteQuery = "DELETE FROM orders WHERE orderID = '" + id + "'";
            stmt.executeUpdate(deleteQuery);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
            // Handle any exception that occurs during the database operation
        }
    }
}
