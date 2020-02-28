public class C {
    private String SELECT_COUNT = "select count(*) from jbid_io_attr  inner join jbid_io_attr_text_values  " +
            "on jbid_io_attr.ATTRIBUTE_ID =jbid_io_attr_text_values.TEXT_ATTR_VALUE_ID where NAME='enabled' and ATTR_VALUE='true'";
    private String SELECT = "select ATTRIBUTE_ID from jbid_io_attr  inner join jbid_io_attr_text_values  " +
            "on jbid_io_attr.ATTRIBUTE_ID =jbid_io_attr_text_values.TEXT_ATTR_VALUE_ID where NAME='enabled' and ATTR_VALUE='true'";
    private final String COLUMN_ID = "ATTRIBUTE_ID";

    private PreparedStatement findItemsStatement = null;
    private PreparedStatement countItemsStatement = null;

    @Override
    public void processUpgrade() {

        ResultSet rs = null;
        ResultSet count = null;
        Connection connection = new Connection('...');
        boolean success = false;
        
        if (connection != null) {
            countItemsStatement = connection.prepareStatement(SELECT_COUNT_ENABLE_ATTRIBUTE);
            count = countItemsStatement.executeQuery();
            if (count.next() && count.getInt(1) > 0) {

                findItemsStatement = connection.prepareStatement(SELECT_ENABLE_ATTRIBUTE);
                findItemsStatement.setFetchSize(1000);
                rs = findItemsStatement.executeQuery();

                StringBuilder temp= new StringBuilder();
                int i = 0;

                    while (rs.next()) {
                        i++;
                        temp.append(rs.getString(COLUMN_ID));
                        if (i==1000) {
                          //do sth
                        }else{
                          //do sth
                        }
                    }
                    if(i!=1000){
                      //do sth
                    }
                }
                success = true;
            }
    }
}
