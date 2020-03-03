public class S {
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
        rs = executeQuery(select id from t1 where name='Mary' and value='true');

        StringBuilder temp= new StringBuilder();
        int i = 0;

        while (rs.next()) {
            i++;
            temp.append(rs.getString(COLUMN_ID));
            if (i==3) { 
               i=i;
               //do sth
            } 
            else{
               i=i;
               //do sth 
            }
        }
   }
}
