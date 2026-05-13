import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;

public class JAVA_COMPUTE_SELECT_WHERE_MF_JavaCompute extends MbJavaComputeNode {

    public void evaluate(MbMessageAssembly inAssembly) throws MbException {

        MbOutputTerminal out = getOutputTerminal("out");
        MbOutputTerminal alt = getOutputTerminal("alternate");

        // MbMessage inMessage = inAssembly.getMessage();

        MbMessage outMessage = new MbMessage();
        MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly, outMessage);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {

            conn = getJDBCType4Connection("xe", JDBC_TransactionType.MB_TRANSACTION_AUTO);

            MbElement inputRoot = inMessage.getRootElement();
            MbElement customerRoot = inputRoot.getFirstElementByPath("XMLNSC/Customer");

            int id = Integer.parseInt(customerRoot.getFirstElementByPath("EMP_ID").getValueAsString() );

            pstmt = conn.prepareStatement( "SELECT EMP_ID, EMP_NAME, EMP_AGE, EMP_CITY FROM EMPLOYEE_TAB WHERE EMP_ID = ?" );

            pstmt.setInt(1, id);

            rs = pstmt.executeQuery();

            MbElement outRoot = outMessage.getRootElement();

            MbElement jsonRoot = outRoot.createElementAsLastChild("JSON");
            MbElement data = jsonRoot.createElementAsLastChild(MbElement.TYPE_NAME, "Data", null);
            MbElement customers = data.createElementAsLastChild(MbElement.TYPE_NAME, "Customers", null);

            boolean found = false;

            while (rs.next()) {
                found = true;

                MbElement customer = customers.createElementAsLastChild(MbElement.TYPE_NAME, "Customer", null);

              //  customer.createElementAsLastChild(MbElement.TYPE_NAME, "ID", rs.getInt("EMP_ID"));
                customer.createElementAsLastChild(MbElement.TYPE_NAME, "NAME", rs.getString("EMP_NAME"));
                customer.createElementAsLastChild(MbElement.TYPE_NAME, "Age", rs.getInt("EMP_AGE"));
                customer.createElementAsLastChild(MbElement.TYPE_NAME, "City", rs.getString("EMP_CITY"));
            }

            if (!found) {
                MbElement msg = data.createElementAsLastChild(MbElement.TYPE_NAME, "Message", null);
                msg.setValue("No Record Found");
            }

            MbElement props = outRoot.getFirstElementByPath("Properties");
            if (props != null) {
                MbElement ct = props.getFirstElementByPath("ContentType");
                if (ct != null) {
                    ct.setValue("application/json");
                }
            }

            out.propagate(outAssembly);

        } catch (Exception e) {

            e.printStackTrace(); 
            alt.propagate(inAssembly);

        } finally {

            try { if (rs != null) rs.close(); } catch (Exception e) {}
            try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
            try { if (conn != null) conn.close(); } catch (Exception e) {}
        }
    }
}
