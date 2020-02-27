public class C {
    private String SELECT_COUNT = "select count(*) from jbid_io_attr  inner join jbid_io_attr_text_values  " +
            "on jbid_io_attr.ATTRIBUTE_ID =jbid_io_attr_text_values.TEXT_ATTR_VALUE_ID where NAME='enabled' and ATTR_VALUE='true'";
    private String SELECT = "select ATTRIBUTE_ID from jbid_io_attr  inner join jbid_io_attr_text_values  " +
            "on jbid_io_attr.ATTRIBUTE_ID =jbid_io_attr_text_values.TEXT_ATTR_VALUE_ID where NAME='enabled' and ATTR_VALUE='true'";
    private final String REMOVE_ENABLE_ATTRIBUTE = "delete from jbid_io_attr where ATTRIBUTE_ID IN ( ";
    private final String REMOVE_ENABLE_ATTRIBUTE_VALUE = "delete from jbid_io_attr_text_values where TEXT_ATTR_VALUE_ID IN (";
    private final String COLUMN_ID = "ATTRIBUTE_ID";

    private PreparedStatement findItemsStatement = null;
    private PreparedStatement countItemsStatement = null;
    private PreparedStatement removeItemStatement = null;
    private PreparedStatement removeValueStatement = null;

    @Override
    public void processUpgrade(String oldVersion, String newVersion) {

        ResultSet rs = null;
        ResultSet count = null;
        Connection connection = new Connection();
        boolean success = false;
        int nb = 0;
        
        if (connection != null) {
            countItemsStatement = connection.prepareStatement(SELECT_COUNT_ENABLE_ATTRIBUTE);
            count = countItemsStatement.executeQuery();
            if (count.next() && count.getInt(1) > 0) {
                nb = count.getInt(1);

                findItemsStatement = connection.prepareStatement(SELECT_ENABLE_ATTRIBUTE);
                findItemsStatement.setFetchSize(1000);
                rs = findItemsStatement.executeQuery();

                StringBuilder temp= new StringBuilder();
                int i = 0;

                    while (rs.next()) {
                        i++;
                        temp.append(rs.getString(COLUMN_ID));
                        if (i % 1000 == 0) {
                            removeBatch(connection, temp.toString());
                            connection.commit();
                            LOG.info("Clean in progress : {}/{}", i, nb);
                            temp = new StringBuilder();
                        }else{
                            temp.append(",");
                        }
                    }
                    if(i% 1000 != 0){
                        removeBatch(connection, temp.substring(0, temp.lastIndexOf(",")));
                        connection.commit();
                        LOG.info("Clean in progress : {}/{}", i, nb);
                    }
                }
                success = true;
            }
    }
}
