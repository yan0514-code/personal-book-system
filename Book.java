import java.io.Serializable;
import java.util.Arrays;

public class Book implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String title;
    private String author;
    private String[] tags;

    public Book(int id, String title, String author, String tagsStr) {
        this.id = id;
        this.title = title;
        this.author = author;
        this.tags = tagsStr.split(";");
    }

    // Getter方法
    public int getId() { return id; }
    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String[] getTags() { return tags; }

    @Override
    public String toString() {
        return "ID: " + id + " | 书名: " + title + " | 作者: " + author + " | 标签: " + Arrays.toString(tags);
    }
}