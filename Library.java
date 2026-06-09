import java.io.*;
import java.util.*;
import com.sun.net.httpserver.*;
import java.net.InetSocketAddress;

class Book {
    int id;
    String name;
    String author;

    Book(int id, String name, String author) {
        this.id = id;
        this.name = name;
        this.author = author;
    }
}

public class Library {

    static List<Book> books = new ArrayList<>();

    // 📂 Load CSV
    public static void loadData() throws Exception {
        BufferedReader br = new BufferedReader(new FileReader("Books_data.csv"));
        String line;

        br.readLine();

        while ((line = br.readLine()) != null) {
            String[] data = line.split(",");
            books.add(new Book(
                Integer.parseInt(data[0]),
                data[1],
                data[2]
            ));
        }
        br.close();
    }

    // 🔄 Insertion Sort
    public static void insertionSort() {
        for (int i = 1; i < books.size(); i++) {
            Book key = books.get(i);
            int j = i - 1;

            while (j >= 0 && books.get(j).name.compareToIgnoreCase(key.name) > 0) {
                books.set(j + 1, books.get(j));
                j--;
            }
            books.set(j + 1, key);
        }
    }

    // 🔍 Search by Name OR Author
    public static List<Book> searchBooks(String keyword) {
        List<Book> result = new ArrayList<>();

        for (Book b : books) {
            if (b.name.equalsIgnoreCase(keyword) ||
                b.author.equalsIgnoreCase(keyword)) {
                result.add(b);
            }
        }
        return result;
    }

    public static void main(String[] args) throws Exception {

        loadData();
        insertionSort();

        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);

        // 🔍 SEARCH (Name OR Author)
        server.createContext("/search", exchange -> {

            String query = exchange.getRequestURI().getQuery();
            String keyword = query.split("=")[1];

            List<Book> result = searchBooks(keyword);

            StringBuilder response = new StringBuilder();

            if (result.size() == 0) {
                response.append("NOT_FOUND");
            } else {
                for (Book b : result) {
                    response.append(b.id).append(",")
                            .append(b.name).append(",")
                            .append(b.author).append("\n");
                }
            }

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            byte[] res = response.toString().getBytes();
            exchange.sendResponseHeaders(200, res.length);

            OutputStream os = exchange.getResponseBody();
            os.write(res);
            os.close();
        });

        // 📊 SHOW ALL
        server.createContext("/books", exchange -> {
            StringBuilder response = new StringBuilder();

            for (Book b : books) {
                response.append(b.id).append(",")
                        .append(b.name).append(",")
                        .append(b.author).append("\n");
            }

            byte[] res = response.toString().getBytes();

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            exchange.sendResponseHeaders(200, res.length);

            OutputStream os = exchange.getResponseBody();
            os.write(res);
            os.close();
        });

        // ➕ ADD
        server.createContext("/add", exchange -> {

            String[] params = exchange.getRequestURI().getQuery().split("&");

            int id = 0;
            String name = "", author = "";

            for (String p : params) {
                String[] pair = p.split("=");
                if (pair[0].equals("id")) id = Integer.parseInt(pair[1]);
                if (pair[0].equals("name")) name = pair[1];
                if (pair[0].equals("author")) author = pair[1];
            }

            books.add(new Book(id, name, author));
            insertionSort();

            String response = "Book Added";

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            byte[] res = response.getBytes();
            exchange.sendResponseHeaders(200, res.length);

            OutputStream os = exchange.getResponseBody();
            os.write(res);
            os.close();
        });

        // 🔄 UPDATE
        server.createContext("/update", exchange -> {

            String[] params = exchange.getRequestURI().getQuery().split("&");

            int id = 0;
            String name = "", author = "";

            for (String p : params) {
                String[] pair = p.split("=");
                if (pair[0].equals("id")) id = Integer.parseInt(pair[1]);
                if (pair[0].equals("name")) name = pair[1];
                if (pair[0].equals("author")) author = pair[1];
            }

            boolean updated = false;

            for (Book b : books) {
                if (b.id == id) {
                    b.name = name;
                    b.author = author;
                    updated = true;
                }
            }

            insertionSort();

            String response = updated ? "Book Updated" : "Book Not Found";

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            byte[] res = response.getBytes();
            exchange.sendResponseHeaders(200, res.length);

            OutputStream os = exchange.getResponseBody();
            os.write(res);
            os.close();
        });

        // ❌ DELETE
        server.createContext("/delete", exchange -> {

            int id = Integer.parseInt(exchange.getRequestURI().getQuery().split("=")[1]);

            boolean removed = books.removeIf(b -> b.id == id);

            String response = removed ? "Book Deleted" : "Book Not Found";

            exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
            byte[] res = response.getBytes();
            exchange.sendResponseHeaders(200, res.length);

            OutputStream os = exchange.getResponseBody();
            os.write(res);
            os.close();
        });

        server.start();
        System.out.println("✅ Server running at http://localhost:8080");
    }
}