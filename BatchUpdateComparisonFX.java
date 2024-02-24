import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import javafx.scene.layout.VBox;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class BatchUpdateComparisonFX extends Application {
    private TextField urlField;
    private TextField usernameField;
    private TextField passwordField;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Batch Update Comparison");

        urlField = new TextField();
        urlField.setPromptText("URL");
        usernameField = new TextField();
        usernameField.setPromptText("Username");
        passwordField = new TextField();
        passwordField.setPromptText("Password");

        Button connectButton = new Button("Connect to Database");
        connectButton.setOnAction(e -> connectToDatabase());

        VBox inputBox = new VBox(10);
        inputBox.setPadding(new Insets(10, 10, 10, 10));
        inputBox.getChildren().addAll(urlField, usernameField, passwordField, connectButton);

        BorderPane borderPane = new BorderPane();
        borderPane.setCenter(inputBox);

        Scene scene = new Scene(borderPane, 300, 200);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void connectToDatabase() {
        String url = urlField.getText();
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            Connection conn = DriverManager.getConnection(url, username, password);
            System.out.println("Connected to database.");

            long startTimeWithoutBatch = System.currentTimeMillis();
            insertRecords(conn, false);
            long endTimeWithoutBatch = System.currentTimeMillis();
            System.out.println("Time taken without batch updates: " + (endTimeWithoutBatch - startTimeWithoutBatch) + " ms");

            long startTimeWithBatch = System.currentTimeMillis();
            insertRecords(conn, true);
            long endTimeWithBatch = System.currentTimeMillis();
            System.out.println("Time taken with batch updates: " + (endTimeWithBatch - startTimeWithBatch) + " ms");

            conn.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void insertRecords(Connection conn, boolean useBatch) throws SQLException {
        String insertQuery = "INSERT INTO Temp(num1, num2, num3) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            for (int i = 0; i < 1000; i++) {
                double num1 = Math.random();
                double num2 = Math.random();
                double num3 = Math.random();

                stmt.setDouble(1, num1);
                stmt.setDouble(2, num2);
                stmt.setDouble(3, num3);

                if (useBatch) {
                    stmt.addBatch();
                } else {
                    stmt.executeUpdate();
                }
            }

            if (useBatch) {
                stmt.executeBatch();
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}