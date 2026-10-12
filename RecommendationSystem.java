import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class RecommendationSystem {
    private static final String BOOKS_FILE = "books.txt";
    private static final String USERS_FILE = "users.txt";
    private List<Book> allBooks;
    private Map<Integer, User> users;
    private int nextUserId;

    public RecommendationSystem() {
        allBooks = new ArrayList<>();
        users = new HashMap<>();
        nextUserId = 1;
        loadData();
    }

    // 添加公共访问方法
    public List<Book> getAllBooks() {
        return allBooks;
    }
    public Map<Integer, User> getUsers() {
        return users;
    }

    // 从文件加载数据
    private void loadData() {
        loadBooks();
        loadUsers();
    }

    // 加载图书数据
    private void loadBooks() {
        File file = new File(BOOKS_FILE);
        if (!file.exists()) {
            initSampleBooks();
            saveBooks();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 4) {
                    int id = Integer.parseInt(parts[0]);
                    String title = parts[1];
                    String author = parts[2];
                    String tagsStr = parts[3];
                    allBooks.add(new Book(id, title, author, tagsStr));
                }
            }
        } catch (IOException e) {
            System.out.println("加载图书数据失败: " + e.getMessage());
            initSampleBooks();
        }
    }

    // 加载用户数据
    private void loadUsers() {
        File file = new File(USERS_FILE);
        if (!file.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length >= 2) {
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    User user = new User(id, name);

                    // 解析已读图书ID
                    if (parts.length >= 3 && !parts[2].isEmpty()) {
                        String[] bookIds = parts[2].split(";");
                        for (String bid : bookIds) {
                            try {
                                user.addReadBook(Integer.parseInt(bid));
                            } catch (NumberFormatException e) {
                                // 忽略无效ID
                            }
                        }
                    }

                    users.put(id, user);
                    if (id >= nextUserId) {
                        nextUserId = id + 1;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("加载用户数据失败: " + e.getMessage());
        }
    }

    // 初始化示例图书数据
    private void initSampleBooks() {
        allBooks.add(new Book(1, "三体", "刘慈欣", "科幻;宇宙;硬科幻"));
        allBooks.add(new Book(2, "银河帝国", "阿西莫夫", "科幻;机器人;帝国"));
        allBooks.add(new Book(3, "基地", "阿西莫夫", "科幻;心理史学;帝国"));
        allBooks.add(new Book(4, "球状闪电", "刘慈欣", "科幻;物理;军事"));
        allBooks.add(new Book(5, "1984", "乔治·奥威尔", "反乌托邦;政治;文学"));
        allBooks.add(new Book(6, "动物庄园", "乔治·奥威尔", "反乌托邦;寓言;政治"));
        allBooks.add(new Book(7, "百年孤独", "马尔克斯", "魔幻现实主义;文学;家族"));
        allBooks.add(new Book(8, "人类简史", "尤瓦尔·赫拉利", "历史;科普;社会"));
        allBooks.add(new Book(9, "未来简史", "尤瓦尔·赫拉利", "未来学;科技;社会"));
        allBooks.add(new Book(10, "黑客帝国", "沃卓斯基姐妹", "科幻;哲学;计算机"));
    }

    // 保存图书数据到文件
    private void saveBooks() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(BOOKS_FILE))) {
            for (Book book : allBooks) {
                String tagsStr = String.join(";", book.getTags());
                writer.write(book.getId() + "," + book.getTitle() + "," +
                        book.getAuthor() + "," + tagsStr + "\n");
            }
        } catch (IOException e) {
            System.out.println("保存图书数据失败: " + e.getMessage());
        }
    }

    // 保存用户数据到文件
    public void saveUsers() {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(USERS_FILE))) {
        for (User user : users.values()) {
            String readBooksStr = String.join(";", user.getReadBooks().stream()
                    .map(Object::toString).collect(Collectors.toList()));
            writer.write(user.getId() + "," + user.getName() + "," + readBooksStr + "\n");
        }
    } catch (IOException e) {
        System.out.println("保存用户数据失败: " + e.getMessage());
    }
}

    // 添加用户已读图书
    public void addUserReadBook(int userId, int bookId) {
        User user = users.get(userId);
        if (user != null) {
            user.addReadBook(bookId);
            saveUsers();
        }
    }

    // 计算两个标签数组的相似度（简化版余弦相似度）
    private double calculateSimilarity(String[] tags1, String[] tags2) {
        Set<String> set1 = new HashSet<>(Arrays.asList(tags1));
        Set<String> set2 = new HashSet<>(Arrays.asList(tags2));

        int intersection = 0;
        for (String tag : set1) {
            if (set2.contains(tag)) {
                intersection++;
            }
        }

        int union = set1.size() + set2.size() - intersection;
        if (union == 0) return 0; // 避免除零错误
        return (double) intersection / union;
    }

    // 根据用户阅读历史推荐图书
    public List<Book> recommendBooks(User user) {
        if (user.getReadBooks().isEmpty()) {
            // 冷启动：推荐热门图书（按标签数量排序）
            return allBooks.stream()
                    .sorted((b1, b2) -> Integer.compare(b2.getTags().length, b1.getTags().length))
                    .limit(5)
                    .collect(Collectors.toList());
        }

        // 获取用户偏好标签
        String[] userTags = user.getPreferTags(allBooks);

        // 计算所有图书与用户偏好的相似度
        List<BookWithSimilarity> rankedBooks = new ArrayList<>();
        for (Book book : allBooks) {
            if (!user.getReadBooks().contains(book.getId())) { // 排除已读图书
                double similarity = calculateSimilarity(userTags, book.getTags());
                if (similarity > 0.2) { // 过滤低相似度图书
                    rankedBooks.add(new BookWithSimilarity(book, similarity));
                }
            }
        }

        // 按相似度降序排序
        rankedBooks.sort((a, b) -> Double.compare(b.getSimilarity(), a.getSimilarity()));

        // 返回前5条推荐
        return rankedBooks.stream()
                .limit(5)
                .map(BookWithSimilarity::getBook)
                .collect(Collectors.toList());
    }

    // 内部类：存储图书及其相似度
    private static class BookWithSimilarity {
        private Book book;
        private double similarity;

        public BookWithSimilarity(Book book, double similarity) {
            this.book = book;
            this.similarity = similarity;
        }

        public Book getBook() { return book; }
        public double getSimilarity() { return similarity; }
    }
}