package com.ccclubs.storage.demo;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ccclubs.common.util.PropertiesHelper;
import com.ccclubs.storage.mysql.MysqlTool;
import com.ccclubs.storage.util.StorageConst;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.sql.*;
import java.util.List;

/**
 * Created by taosm on 2018/6/7.
 */
public class MysqlHelper {
    private static Logger logger = Logger.getLogger(MysqlHelper.class);
    private static MysqlHelper mysqlHelper = null;
    private DruidDataSource druidDataSource = null;

    private MysqlHelper(){}

    public synchronized static MysqlHelper getInstance(){
        if(mysqlHelper==null){
            mysqlHelper=new MysqlHelper();
            mysqlHelper.doInit();
        }
        return mysqlHelper;
    }

    private void doInit(){
        PropertiesHelper propertiesHelper = PropertiesHelper.getInstance();
        String MYSQL_INITIALSIZE=propertiesHelper.getValue(StorageConst.MYSQL_INITIALSIZE_KEY);
        String MYSQL_MAXACTIVE=propertiesHelper.getValue(StorageConst.MYSQL_MAXACTIVE_KEY);
        String MYSQL_MAXWAIT=propertiesHelper.getValue(StorageConst.MYSQL_MAXWAIT_KEY);
        String MYSQL_VALIDATIONQUERY=propertiesHelper.getValue(StorageConst.MYSQL_VALIDATIONQUERY_KEY);
        String MYSQL_TESTONBORROW=propertiesHelper.getValue(StorageConst.MYSQL_TESTONBORROW_KEY);
        String MYSQL_TESTONRETURN=propertiesHelper.getValue(StorageConst.MYSQL_TESTONRETURN_KEY);
        String MYSQL_POOLPREPAREDSTATEMENTS=propertiesHelper.getValue(StorageConst.MYSQL_POOLPREPAREDSTATEMENTS_KEY);
        String MYSQL_MAXPOOLPREPAREDSTATEMENTPERCONNECTIONSIZE=propertiesHelper.getValue(StorageConst.MYSQL_MAXPOOLPREPAREDSTATEMENTPERCONNECTIONSIZE_KEY);

        druidDataSource = new DruidDataSource();
        druidDataSource.setDriverClassName("com.mysql.jdbc.Driver");
        druidDataSource.setUrl("jdbc:mysql://"+"127.0.0.1"+":"+"3306"+"/"+"metrics");
        druidDataSource.setUsername("root");
        druidDataSource.setPassword("123456");
        druidDataSource.setInitialSize(Integer.parseInt(StringUtils.deleteWhitespace(MYSQL_INITIALSIZE)));
        druidDataSource.setMaxActive(Integer.parseInt(StringUtils.deleteWhitespace(MYSQL_MAXACTIVE)));
        druidDataSource.setMaxWait(Integer.parseInt(StringUtils.deleteWhitespace(MYSQL_MAXWAIT)));
        druidDataSource.setValidationQuery(StringUtils.deleteWhitespace(MYSQL_VALIDATIONQUERY));
        druidDataSource.setTestOnBorrow(Boolean.valueOf(StringUtils.deleteWhitespace(MYSQL_TESTONBORROW)));
        druidDataSource.setTestOnReturn(Boolean.valueOf(StringUtils.deleteWhitespace(MYSQL_TESTONRETURN)));
        druidDataSource.setPoolPreparedStatements(Boolean.valueOf(StringUtils.deleteWhitespace(MYSQL_POOLPREPAREDSTATEMENTS)));
        druidDataSource.setMaxPoolPreparedStatementPerConnectionSize(Integer.parseInt(StringUtils.deleteWhitespace(MYSQL_MAXPOOLPREPAREDSTATEMENTPERCONNECTIONSIZE)));

    }

    public Connection getConnection(){
        Connection connection = null;
        try {
            if (druidDataSource != null) {
                connection = druidDataSource.getConnection();
                connection.setAutoCommit(false);
            }
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        return connection;
    }

    public JSONArray queryRecords(String sql){
        JSONArray jsonArray = new JSONArray();
        PreparedStatement pst = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            pst = connection.prepareStatement(sql);
            rs = pst.executeQuery();
            ResultSetMetaData metaData = rs.getMetaData();
            JSONObject obj = null;
            while (rs.next()) {
                obj = new JSONObject();
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    Object columnValue = rs.getObject(columnName);
                    obj.put(columnName, columnValue);
                }
                jsonArray.add(obj);
            }
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        finally {
            if(rs!=null){
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }
            }
            if(pst!=null){
                try {
                    pst.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }
            }
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }
            }
        }
        return jsonArray;
    }


    public void deleteRecords(String sql) throws Exception {
        PreparedStatement pst = null;
        ResultSet rs = null;
        Connection connection = null;
        try {
            connection = getConnection();
            pst = connection.prepareStatement(sql);
            pst.executeUpdate();
            connection.commit();
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        finally {
            if(pst!=null){
                try {
                    pst.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }
            }
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    public void insertRecords(List<String> sql_list)throws SQLException{
        ResultSet rs = null;
        Connection connection = null;
        Statement st = null;
        try {
            connection = getConnection();
            st = connection.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_READ_ONLY);
            for (String sql : sql_list) {
                st.addBatch(sql);
            }
            st.executeBatch();
            connection.commit();
        }
        catch (Exception ex){
            logger.error(ex.getMessage());
        }
        finally {
            if(st!=null){
                try {
                    st.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }
            }
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }
            }
        }
    }

    public void insertRecords(PreparedStatement preparedStatement){
        Connection connection = null;
        try {
            if(preparedStatement!=null) {
                connection = preparedStatement.getConnection();
                preparedStatement.executeBatch();
                connection.commit();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        finally {
            if(preparedStatement!=null){
                try {
                    preparedStatement.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if(connection!=null){
                try {
                    connection.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void doPrepareStatementSet(PreparedStatement preparedStatement,Integer parameterIndex,Object value){
        try {
            if(value==null){
                preparedStatement.setNull(parameterIndex,Types.NULL);
            }
            else{
                String fullTypeName = value.getClass().getTypeName();
                int lastIndex = fullTypeName.lastIndexOf(".");
                String typeName = fullTypeName.substring(lastIndex+1);
                if("Integer".equalsIgnoreCase(typeName)){
                    preparedStatement.setInt(parameterIndex,Integer.parseInt(value.toString()));
                }
                else if("Double".equalsIgnoreCase(typeName)){
                    preparedStatement.setDouble(parameterIndex,Double.parseDouble(value.toString()));
                }
                else if("Float".equalsIgnoreCase(typeName)){
                    preparedStatement.setFloat(parameterIndex,Float.parseFloat(value.toString()));
                }
                else if("Long".equalsIgnoreCase(typeName)){
                    preparedStatement.setLong(parameterIndex,Long.parseLong(value.toString()));
                }
                else if("String".equalsIgnoreCase(typeName)){
                    preparedStatement.setString(parameterIndex,value.toString());
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
    }

    public static void main(String[] args) {

    }
}
