import com.ibm.broker.javacompute.MbJavaComputeNode;
import com.ibm.broker.plugin.*;
import java.sql.*;


public class JAVA_COMPUTE_UPDATE_ALL_AND_WHERE_MF_JavaCompute extends MbJavaComputeNode {

	public void evaluate(MbMessageAssembly inAssembly) throws MbException {
		MbOutputTerminal out = getOutputTerminal("out");

		MbMessage outMessage = new MbMessage();
		MbMessageAssembly outAssembly = new MbMessageAssembly(inAssembly, outMessage);
		
		Connection conn = null;
		PreparedStatement pstmt = null;
		String messageText = "";
		

		try {
			conn = getJDBCType4Connection("xe", JDBC_TransactionType.MB_TRANSACTION_AUTO);
			
			MbMessage inMessage = inAssembly.getMessage();
			MbElement inputRoot = inMessage.getRootElement();
			MbElement empRoot = inputRoot.getFirstElementByPath("XMLNSC/Employee");
			int id = Integer.parseInt(empRoot.getFirstElementByPath("ID").getValueAsString());
			
		//	pstmt = conn.prepareStatement("UPDATE EMPLOYEE_TAB SET EMP_CITY = 'BOBBILI' ");
			pstmt = conn.prepareStatement("UPDATE EMPLOYEE_TAB SET EMP_NAME = 'anjali' WHERE EMP_ID = ? ");
			pstmt.setInt(1, id);

			int rowsAffected = pstmt.executeUpdate();

			if (rowsAffected > 0) {
			    messageText = "Updated successfully. Rows updated: " + rowsAffected;
			} else {
			    messageText = "No rows Updated";
			}

			MbElement outRoot = outMessage.getRootElement();
			MbElement xmlRoot = outRoot.createElementAsLastChild("XMLNSC");
			MbElement Result = xmlRoot.createElementAsLastChild(MbElement.TYPE_NAME, "Result", null);

			Result.createElementAsLastChild(MbElement.TYPE_NAME, "message", messageText);
			
			out.propagate(outAssembly);
			} 
			catch (Exception e) {throw new MbUserException(this, "evaluate()", "", "", e.toString(), null); }
			finally {

						try { if (pstmt != null) pstmt.close(); } catch (Exception e) {}
						try { if (conn != null) conn.close(); } catch (Exception e) {}
					}
	}
}


