import java.io.*;
import java.util.*;

// 1. SINGLETON

class ConfigurationManager {
    private static volatile ConfigurationManager instance;
    private Map<String, String> settings = new HashMap<>();
    private static final Object lock = new Object();

    private ConfigurationManager() {
        loadFromFile("config.txt");
    }

    public static ConfigurationManager getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new ConfigurationManager();
                }
            }
        }
        return instance;
    }

    public void setSetting(String key, String value) {
        settings.put(key, value);
    }

    public String getSetting(String key) {
        if (!settings.containsKey(key)) {
            throw new RuntimeException("Настройка не найдена: " + key);
        }
        return settings.get(key);
    }

    public void loadFromFile(String filename) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=");
                if (parts.length == 2) {
                    settings.put(parts[0], parts[1]);
                }
            }
        } catch (IOException e) {
            System.out.println("Файл конфигурации не найден, создается новый.");
        }
    }

    public void saveToFile(String filename) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
            for (Map.Entry<String, String> entry : settings.entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Ошибка сохранения файла.");
        }
    }
}


// 2. BUILDER

class Report {
    String header;
    String content;
    String footer;

    public void show() {
        System.out.println(header);
        System.out.println(content);
        System.out.println(footer);
        System.out.println("--------------------");
    }
}

interface IReportBuilder {
    void setHeader(String header);
    void setContent(String content);
    void setFooter(String footer);
    Report getReport();
}

class TextReportBuilder implements IReportBuilder {
    private Report report = new Report();

    public void setHeader(String header) {
        report.header = "TEXT HEADER: " + header;
    }

    public void setContent(String content) {
        report.content = "TEXT CONTENT: " + content;
    }

    public void setFooter(String footer) {
        report.footer = "TEXT FOOTER: " + footer;
    }

    public Report getReport() {
        return report;
    }
}

class HtmlReportBuilder implements IReportBuilder {
    private Report report = new Report();

    public void setHeader(String header) {
        report.header = "<h1>" + header + "</h1>";
    }

    public void setContent(String content) {
        report.content = "<p>" + content + "</p>";
    }

    public void setFooter(String footer) {
        report.footer = "<footer>" + footer + "</footer>";
    }

    public Report getReport() {
        return report;
    }
}

class ReportDirector {
    public void constructReport(IReportBuilder builder) {
        builder.setHeader("Отчет за день");
        builder.setContent("Продажи и статистика");
        builder.setFooter("Конец отчета");
    }
}


// 3. PROTOTYPE

class Product implements Cloneable {
    String name;
    double price;
    int quantity;

    public Product(String name, double price, int quantity) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public Product clone() {
        try {
            return (Product) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

class Discount implements Cloneable {
    String name;
    double percent;

    public Discount(String name, double percent) {
        this.name = name;
        this.percent = percent;
    }

    public Discount clone() {
        try {
            return (Discount) super.clone();
        } catch (CloneNotSupportedException e) {
            return null;
        }
    }
}

class Order implements Cloneable {
    List<Product> products = new ArrayList<>();
    List<Discount> discounts = new ArrayList<>();
    double deliveryCost;
    String paymentMethod;

    public void addProduct(Product p) {
        products.add(p);
    }

    public void addDiscount(Discount d) {
        discounts.add(d);
    }

    public Order clone() {
        Order cloned = new Order();
        cloned.deliveryCost = this.deliveryCost;
        cloned.paymentMethod = this.paymentMethod;

        for (Product p : products) {
            cloned.products.add(p.clone());
        }

        for (Discount d : discounts) {
            cloned.discounts.add(d.clone());
        }

        return cloned;
    }

    public void showOrder() {
        System.out.println("Метод оплаты: " + paymentMethod);
        System.out.println("Доставка: " + deliveryCost);

        System.out.println("Товары:");
        for (Product p : products) {
            System.out.println(p.name + " | Цена: " + p.price + " | Кол-во: " + p.quantity);
        }

        System.out.println("Скидки:");
        for (Discount d : discounts) {
            System.out.println(d.name + " - " + d.percent + "%");
        }

        System.out.println("--------------------");
    }
}


// MAIN (ТЕСТ)

public class Main {
    public static void main(String[] args) {

        // Singleton тест
        ConfigurationManager config1 = ConfigurationManager.getInstance();
        ConfigurationManager config2 = ConfigurationManager.getInstance();

        config1.setSetting("theme", "dark");
        System.out.println("Config2 theme: " + config2.getSetting("theme"));

        config1.saveToFile("config.txt");

        // Builder тест
        ReportDirector director = new ReportDirector();

        IReportBuilder textBuilder = new TextReportBuilder();
        director.constructReport(textBuilder);
        Report textReport = textBuilder.getReport();
        textReport.show();

        IReportBuilder htmlBuilder = new HtmlReportBuilder();
        director.constructReport(htmlBuilder);
        Report htmlReport = htmlBuilder.getReport();
        htmlReport.show();

        // Prototype тест
        Order originalOrder = new Order();
        originalOrder.paymentMethod = "Карта";
        originalOrder.deliveryCost = 1500;

        originalOrder.addProduct(new Product("Ноутбук", 500000, 1));
        originalOrder.addProduct(new Product("Мышка", 10000, 2));
        originalOrder.addDiscount(new Discount("Скидка студента", 10));

        Order clonedOrder = originalOrder.clone();
        clonedOrder.paymentMethod = "Наличные";

        originalOrder.showOrder();
        clonedOrder.showOrder();
    }
}
