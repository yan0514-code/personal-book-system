import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private int id;
    private String name;
    private List<Integer> readBooks;

    public User(int id, String name) {
        this.id = id;
        this.name = name;
        this.readBooks = new ArrayList<>();
    }

    // Getter和Setter方法
    public int getId() { return id; }
    public String getName() { return name; }
    public List<Integer> getReadBooks() { return readBooks; }

    public void addReadBook(int bookId) {
        if (!readBooks.contains(bookId)) {
            readBooks.add(bookId);
        }
    }

    // 获取用户偏好标签
    public String[] getPreferTags(List<Book> allBooks) {
        List<String> allTags = new ArrayList<>();
        for (int bookId : readBooks) {
            for (Book book : allBooks) {
                if (book.getId() == bookId) {
                    allTags.addAll(Arrays.asList(book.getTags()));
                    break;
                }
            }
        }
        return allTags.stream().distinct().toArray(String[]::new);
    }

    @Override
    public String toString() {
        return "用户ID: " + id + " | 用户名: " + name + " | 已读图书: " + readBooks;
    }
}