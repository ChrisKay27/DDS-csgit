package results;
import java.sql.*;

/**
 * Created by Chris on 6/16/2016.
 */
public class DBConnection {

    public static void insertResults(ExperimentResults results) {
        Connection conn = null;

        try {
            conn =
                    DriverManager.getConnection("jdbc:mysql://localhost/ddb_results?" +
                            "user=Mani&password=thesis");


            PreparedStatement statement = conn.prepareStatement("INSERT INTO results(experimentNumber,pcot,deadlockDetectionProtocol,deadlockResolutionProtocol," +
                    "topology,arrivalRate,priorityProtocol,numPages,detectionInterval) VALUES (?,?,?,?,?,?,?,?,?)");
            statement.setLong(1,results.getExpNum());
            statement.setDouble(2,results.getPCOT());
            statement.setString(3,results.getDDP());
            statement.setString(4,results.getDRP());
            statement.setString(5,results.getTopology());
            statement.setInt(6,results.getArrivalRate());
            statement.setString(7,results.getPP());
            statement.setInt(8,results.getNumPages());
            statement.setInt(9,results.getDetectInterval());
            statement.execute();


        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
        }
    }
}
