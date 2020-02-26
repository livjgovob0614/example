
public class FolderInfo {

    /**
     * Primary key of folder.
     */
    public long id;

    /**
     * Name of the folder.
     */
    public String name;

    /**
     * Count of solved puzzles in the folder.
     */
    public int solvedCount;

    /**
     * Count of puzzles in "playing" state in the folder.
     */
    public int playingCount;


    public FolderInfo(long id, String name) {
        this.id = id;
        this.name = name;
    }


}
