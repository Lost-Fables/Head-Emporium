package co.lotc.heademporium.sqlite;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;

import co.lotc.heademporium.HeadEmporium; // import your main class
import co.lotc.heademporium.HeadRequest;

public class ReqsSQL extends Database{
	private String dbname;
	private String SQLiteTokensTable;

	public ReqsSQL(HeadEmporium instance){
		super(instance, "reqs_table");
		dbname = instance.getConfig().getString("SQLite.Filename", "heads");
		SQLiteTokensTable = "CREATE TABLE IF NOT EXISTS " + SQLiteTableName + " (\n" +
							"    ID INTEGER PRIMARY KEY,\n" +
							"    APPROVER TEXT,\n" +
							"    REQUESTER TEXT NOT NULL,\n" +
							"    TEXTURE TEXT NOT NULL,\n" +
							"    AMOUNT INTEGER NOT NULL\n" +
							");";
	}


	// SQL creation stuff, You can leave the below stuff untouched.
	public Connection getSQLConnection() {
		File dataFolder = new File(plugin.getDataFolder(), dbname + ".db");
		if (!dataFolder.exists()){
			try {
				dataFolder.createNewFile();
			} catch (IOException e) {
				plugin.getLogger().log(Level.SEVERE, "File write error: " + dbname + ".db");
			}
		}
		try {
			if (connection != null && !connection.isClosed()) {
				return connection;
			}
			Class.forName("org.sqlite.JDBC");
			String locale = dataFolder.toString();
			if (HeadEmporium.DEBUGGING) {
				plugin.getLogger().info("LOCALE: " + locale);
			}
			connection = DriverManager.getConnection("jdbc:sqlite:" + locale);
			return connection;
		} catch (SQLException ex) {
			if (HeadEmporium.DEBUGGING) {
				plugin.getLogger().log(Level.SEVERE, "SQLite exception on initialize", ex);
			}
		} catch (ClassNotFoundException ex) {
			if (HeadEmporium.DEBUGGING) {
				plugin.getLogger().log(Level.SEVERE, "You need the SQLite JBDC library. Google it. Put it in /lib folder.");
			}
		}
		return null;
	}

	public void load() {
		connection = getSQLConnection();
		try {
			Statement s = connection.createStatement();
			s.execute(SQLiteTokensTable);
			s.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		initialize();
	}

	public void initialize(){
		connection = getSQLConnection();
		try {
			String stmt;
			stmt = "SELECT * FROM " + SQLiteTableName + ";";
			PreparedStatement ps = connection.prepareStatement(stmt);
			ResultSet rs = ps.executeQuery();
			while(rs.next()) {
				new HeadRequest(rs.getInt("ID"),
								rs.getString("REQUESTER"),
								rs.getString("TEXTURE"),
								rs.getInt("AMOUNT"),
								rs.getString("APPROVER"));
			}
			close(ps, rs);
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, "Unable to retreive connection", ex);
		}
	}

	// Save info
	public void setToken(int id, String catOrApprov, String nameOrReq, String texture, float priceOrAmount) {
		Connection conn = null;
		PreparedStatement ps = null;
		String stmt;
		stmt = "INSERT INTO " + SQLiteTableName + " (ID,APPROVER,REQUESTER,TEXTURE,AMOUNT) VALUES(?,?,?,?,?)";

		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement(stmt);
			ps.setInt(1, id);
			ps.setString(2, catOrApprov);
			ps.setString(3, nameOrReq);
			ps.setString(4, texture);
			ps.setInt(5, (int) priceOrAmount);
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
	}

	// Get IDs by Texture
	public ArrayList<Integer> getTokenIDsByTexture(String texture) {
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		ArrayList<Integer> ids = new ArrayList<>();

		try {
			conn = getSQLConnection();
			String stmt = "SELECT * FROM " + SQLiteTableName + ";";
			ps = conn.prepareStatement(stmt);
			rs = ps.executeQuery();
			while(rs.next()) {
				if (rs.getString("TEXTURE").equals(texture)) {
					ids.add(rs.getInt("ID"));
				}
			}
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
				if (rs != null)
					rs.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}

		return ids;
	}

	// Purge Requests
	public String purge() {
		Connection conn = null;
		PreparedStatement ps = null;
		String stmt;
		stmt = "DELETE FROM " + SQLiteTableName + ";";

		try {
			conn = getSQLConnection();
			ps = conn.prepareStatement(stmt);
			ps.executeUpdate();
		} catch (SQLException ex) {
			plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionExecute(), ex);
		} finally {
			try {
				if (ps != null)
					ps.close();
			} catch (SQLException ex) {
				plugin.getLogger().log(Level.SEVERE, Errors.sqlConnectionClose(), ex);
			}
		}
		return "All requests have been purged.";
	}

}