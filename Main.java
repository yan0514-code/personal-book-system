import java.util.Scanner;
import java.util.List;

public class Main {
    private static RecommendationSystem system;
    private static Scanner scanner;
    private static User currentUser; // 存储当前用户

    public static void main(String[] args) {
        system = new RecommendationSystem();
        scanner = new Scanner(System.in);
        // 从RecommendationSystem中获取用户（这里取ID为1的用户）
        currentUser = system.getUsers().get(1);
        // 如果users.txt中没有用户，就创建一个默认用户并加入system
        if (currentUser == null) {
            currentUser = new User(1, "DefaultUser");
            system.getUsers().put(1, currentUser);
            system.saveUsers(); // 保存到文件
        }

        System.out.println("=== 个性化图书推荐系统 ===");

        while (true) {
            showMainMenu();
            int choice = getIntInput("请选择操作: ");

            switch (choice) {
                case 1:
                    addReadBook(currentUser);
                    break;
                case 2:
                    viewReadBooks(currentUser);
                    break;
                case 3:
                    getRecommendations(currentUser);
                    break;
                case 4:
                    showAllBooks();
                    break;
                case 0:
                    System.out.println("感谢使用，再见！");
                    scanner.close();
                    return;
                default:
                    System.out.println("无效选择，请重新输入！");
            }
        }
    }

    // 显示主菜单
    private static void showMainMenu() {
        System.out.println("\n===== 主菜单 =====");
        System.out.println("1. 添加已读图书");
        System.out.println("2. 查看已读图书");
        System.out.println("3. 获取图书推荐");
        System.out.println("4. 查看所有图书");
        System.out.println("0. 退出系统");
    }

    // 添加已读图书
    private static void addReadBook(User user) {
        System.out.println("\n===== 添加已读图书 =====");
        showAllBooks();

        int bookId = getIntInput("请输入图书ID (0取消): ");
        if (bookId == 0) return;

        boolean bookExists = false;
        for (Book book : system.getAllBooks()) {
            if (book.getId() == bookId) {
                bookExists = true;
                system.addUserReadBook(user.getId(), bookId);
                System.out.println("已添加《" + book.getTitle() + "》到已读列表！");
                break;
            }
        }

        if (!bookExists) {
            System.out.println("图书ID不存在！");
        }
    }

    // 查看已读图书
    private static void viewReadBooks(User user) {
        System.out.println("\n===== 已读图书 =====");
        List<Integer> readBooks = user.getReadBooks();

        if (readBooks.isEmpty()) {
            System.out.println("你还没有添加已读图书！");
            return;
        }

        for (int bookId : readBooks) {
            for (Book book : system.getAllBooks()) {
                if (book.getId() == bookId) {
                    System.out.println(book);
                    break;
                }
            }
        }
    }

    // 获取推荐图书
    private static void getRecommendations(User user) {
        System.out.println("\n===== 为你推荐的图书 =====");
        List<Book> recommendations = system.recommendBooks(user);

        if (recommendations.isEmpty()) {
            System.out.println("暂无推荐图书！");
            return;
        }

        for (int i = 0; i < recommendations.size(); i++) {
            Book book = recommendations.get(i);
            System.out.println((i + 1) + ". " + book.getTitle() +
                    " | 作者: " + book.getAuthor() +
                    " | 标签: " + String.join("; ", book.getTags()));
        }
    }

    // 显示所有图书
    private static void showAllBooks() {
        System.out.println("\n===== 所有图书 =====");
        if (system.getAllBooks().isEmpty()) {
            System.out.println("暂无图书数据！");
            return;
        }

        for (Book book : system.getAllBooks()) {
            System.out.println(book);
        }
    }

    // 获取整数输入
    private static int getIntInput(String prompt) {
        while (true) {
            try {
                System.out.print(prompt);
                return Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                System.out.println("输入无效，请输入一个整数！");
            }
        }
    }
}