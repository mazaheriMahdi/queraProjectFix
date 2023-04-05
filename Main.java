

 class Main {
    public static void main(String[] args) {
        // TODO: 4/3/23 add email field to customer
        // TODO: 4/3/23 convert router to enum
        ProductController.addProduct(new Car("Mercerises", 12, ProductCondition.AVAILABLE,
                "us", 12, true, 3));
        ProductController.addProduct(new Car("BMW", 12, ProductCondition.AVAILABLE,
                "us", 12, true, 3));
        ProductController.addProduct(new NoteBook("To do List", 120, ProductCondition.AVAILABLE, "us", 120, "normal", 10));
        ProductController.addProduct(new USB("Apecer", 20, ProductCondition.AVAILABLE, 2, 2, 2, 2, 32, 3.2, 14));
        CustomerController.addCustomer(new Customer("mahdi83mazaheri@gmail.com", "mahdi", "123", "mahdi", 9123, "asdasd"));
        CommentController.getComments().add(new Comment(CustomerController.getCurrentCustomer(), 1, "sadfdsfadsf", false));

        Router.rout(8);


    }
}

 class AdminController {
    private static final ArrayList<Admin> admins;

    static {
        admins = new ArrayList<>();
    }


    public static void addAdmin(Admin admin) {
        admins.add(admin);
    }

    public static ArrayList<Admin> getAdmins() {
        return admins;
    }

    public static ArrayList<Application> getApplications() {
        return ApplicationController.getApplications();
    }


    public static void acceptApplication(int applicationId) throws ObjectDoesNotExist {
        Application application = ApplicationController.getById(applicationId);
        application.setCondition(Condition.ACCEPTED);
        if (application.getObject() instanceof Point) {
            ((Point) application.getObject()).setCondition(Condition.ACCEPTED);
        } else if (application.getObject() instanceof Comment) {
            ((Comment) application.getObject()).setCondition(Condition.ACCEPTED);
        } else if (application.getObject() instanceof Payment payment) {
            payment.setCondition(Condition.ACCEPTED);
            payment.getCustomer().setCredit(payment.getCustomer().getCredit() + payment.getAmount());
        } else if (application.getObject() instanceof Customer) {
            CustomerController.addCustomer((Customer) application.getObject());
        }
    }

    public static void rejectApplication(int applicationId) throws ObjectDoesNotExist {
        Application application = ApplicationController.getById(applicationId);
        application.setCondition(Condition.REJECTED);
        if (application.getObject() instanceof Point) {
            ((Point) application.getObject()).setCondition(Condition.REJECTED);
        } else if (application.getObject() instanceof Comment) {
            ((Comment) application.getObject()).setCondition(Condition.REJECTED);
        } else if (application.getObject() instanceof Payment) {
            ((Payment) application.getObject()).setCondition(Condition.REJECTED);
        }
    }

    public static Admin getByUserName(String username) throws ObjectDoesNotExist {
        for (Admin admin : admins) {
            if (Objects.equals(admin.getUserName(), username)) {
                return admin;
            }
        }
        throw new ObjectDoesNotExist("Admin");
    }

    public static ArrayList<Customer> getUserList() {
        return CustomerController.getCustomers();
    }


}


 class ProductController {
    private static final ArrayList<Object> products;
    private static Object currentProduct;

    static {
        products = new ArrayList<>();
    }

    public static Object getCurrentProduct() {
        return currentProduct;
    }

    public static void setCurrentProduct(Object currentProduct) {
        ProductController.currentProduct = currentProduct;
    }

    public static ArrayList<Object> getProducts() {
        return products;
    }


    public static void addProduct(Object object) {
        products.add(object);
    }

    public static void removeProduct(Object object) {
        products.remove(object);
    }

    public static void changeProperties(Object object, Object newObject) {
        int index = products.indexOf(object);
        products.set(index, newObject);
    }

    public static float getPoint(int id) throws ObjectDoesNotExist {
        float pointNumber = 0;
        int number = 0;
        for (Point point : PointController.getPoints()) {

            if (point.getProductId() == id && point.getCondition().equals(Condition.ACCEPTED)) {
                number++;
                pointNumber += point.getPoint();
            }
        }
        return pointNumber / number;
    }

    public static Object getProductById(int id) throws ObjectDoesNotExist {
        for (Object obj : products) {
            if (((BaseProduct) obj).getId() == id) {
                return obj;
            }
        }
        throw new ObjectDoesNotExist("product");
    }


}


 class PointController {
    private static final ArrayList<Point> points;

    static {
        points = new ArrayList<>();
    }

    public static ArrayList<Point> getPoints() {
        return points;
    }

    public static Point addPoint(int point) {
        Point point1 = new Point(CustomerController.getCurrentCustomer(), point, ((BaseProduct) ProductController.getCurrentProduct()).getId());
        points.add(point1);
        return point1;
    }

}


 class CustomerController {
    private static ArrayList<Customer> customers;
    private static Customer currentCustomer;
    private static int currentCustomerIndex;

    static {
        customers = new ArrayList<>();
    }

    //------functions---------------
    public static void addCustomer(Customer customer) {
        customers.add(customer);
    }

    public static void removeCustomer(Customer customer) {
        customers.remove(customer);
    }


    public static void changePersonalInfo(Customer customer) {
        customers.set(currentCustomerIndex, customer);

    }


    public static void addToCart(BaseProduct product, int quantity) throws ObjectDoesNotExist, InsufficientInventory {
        boolean isFound = false;
        if (product.getCount() >= quantity) {
            for (Integer cartItem : currentCustomer.getCart()) {
                CartItem currentItem = CartItemController.getById(cartItem);
                if (currentItem.getProductId() == product.getId()) {
                    isFound = true;
                    currentItem.setQuantity(currentItem.getQuantity() + quantity);
                }
            }
            if (!isFound) {
                CartItem cartItem = CartItemController.addCartItem(product, quantity);
                currentCustomer.getCart().add(cartItem.getId());
            }

        } else throw new InsufficientInventory();
    }


    public static Customer getByUserName(String username) throws ObjectDoesNotExist {
        for (Customer customer : customers) {
            if (Objects.equals(customer.getUserName(), username)) {
                return customer;
            }
        }
        throw new ObjectDoesNotExist("Customer");
    }

    public static ArrayList<Invoice> getHistory() throws ObjectDoesNotExist {
        ArrayList<Invoice> history = new ArrayList<>();
        for (int invoiceId : currentCustomer.getHistory()) {
            history.add(InvoiceController.getById(invoiceId));
        }
        return history;
    }

    public static void addComment(BaseProduct baseProduct, String text) throws ObjectDoesNotExist {
        Comment comment1 = new Comment(currentCustomer, baseProduct.getId(), text, isBuyer(baseProduct));
        CommentController.getComments().add(comment1);
    }

    public static void setPoint(BaseProduct baseProduct, int point) throws CustomerIsNotBuyer, ObjectDoesNotExist {
        Point newPoint = new Point(currentCustomer, baseProduct.getId(), point);
        if (isBuyer(baseProduct)) {
            PointController.getPoints().add(newPoint);
        } else {
            throw new CustomerIsNotBuyer();
        }
    }

    public static void addCredit(float amount) {
        currentCustomer.setCredit(currentCustomer.getCredit() + amount);
    }

    public static ArrayList<CartItem> cart() throws ObjectDoesNotExist {
        ArrayList<CartItem> temp = new ArrayList<>();
        for (int cartItemId : currentCustomer.getCart()) {
            temp.add(CartItemController.getById(cartItemId));
        }
        return temp;
    }


    public static boolean isBuyer(BaseProduct baseProduct) throws ObjectDoesNotExist {
        for (int invoiceId : currentCustomer.getHistory()) {
            Invoice invoice = InvoiceController.getById(invoiceId);
            for (int cartItemId : invoice.getCartItems()) {
                CartItem cartItem = CartItemController.getById(cartItemId);
                if (cartItem.getProductId() == baseProduct.getId()) {
                    return true;
                }
            }
        }
        return false;
    }


    public static void login(Customer customer, String password) throws WrongPassWordException {
        if (Objects.equals(customer.getPassword(), password)) {
            currentCustomerIndex = customers.indexOf(customer);
            currentCustomer = customers.get(currentCustomerIndex);
        } else {
            throw new WrongPassWordException();
        }
    }


    public static void buy() throws ObjectDoesNotExist, CreditNotEnoughException {
        ArrayList<Integer> cartItems = new ArrayList<>();
        float totalAmount = 0F;
        for (CartItem cartItem : cart()) {
            cartItems.add(cartItem.getId());
            totalAmount += cartItem.getUnitPrice() * cartItem.getQuantity();
            ((BaseProduct) ProductController.getProductById(cartItem.getProductId())).reduceCount(cartItem.getQuantity());
        }

        if (currentCustomer.getCredit() >= totalAmount) {
            currentCustomer.addToHistory(InvoiceController.addInvoice(cartItems));
            currentCustomer.getCart().clear();
            currentCustomer.setCredit(currentCustomer.getCredit() - totalAmount);

        } else throw new CreditNotEnoughException();
    }


    //-----getter and setter -------
    public static ArrayList<Object> getProducts() {
        return ProductController.getProducts();
    }

    public static ArrayList<Object> getProducts(String searchPhrase) {
        String regex = ".*" + searchPhrase + ".*";
        Pattern pattern = Pattern.compile(regex);
        ArrayList<Object> temp = new ArrayList<>();
        for (Object obj : ProductController.getProducts()) {
            Matcher matcher = pattern.matcher(((BaseProduct) obj).getName());
            if (matcher.find()) {
                temp.add(obj);
            }
        }
        return temp;
    }


    public static int getCurrentCustomerIndex() {
        return currentCustomerIndex;
    }

    public static void setCurrentCustomerIndex(int currentCustomerIndex) {
        CustomerController.currentCustomerIndex = currentCustomerIndex;
    }

    public static Customer getCurrentCustomer() {
        return currentCustomer;
    }

    public static void setCurrentCustomer(Customer currentCustomer) {
        CustomerController.currentCustomer = currentCustomer;
        currentCustomerIndex = customers.indexOf(currentCustomer);
    }

    public static ArrayList<Customer> getCustomers() {
        return customers;
    }

    public static void setCustomers(ArrayList<Customer> customers) {
        CustomerController.customers = customers;
    }
}


 class InvoiceController {

    private static ArrayList<Invoice> invoices;

    static {
        invoices = new ArrayList<>();
    }

    public static Invoice addInvoice(ArrayList<Integer> cartItems) throws ObjectDoesNotExist {
        Invoice invoice = new Invoice(cartItems);
        invoices.add(invoice);
        return invoice;
    }

    public static ArrayList<Invoice> getInvoices() {
        return invoices;
    }

    public static void setInvoices(ArrayList<Invoice> invoices) {
        InvoiceController.invoices = invoices;
    }

    public static Invoice getById(int id) throws ObjectDoesNotExist {
        for (Invoice invoice : invoices) {
            if (invoice.getId() == id) {

                return invoice;
            }
        }
        throw new ObjectDoesNotExist("invoice");
    }


}


 class CommentController {
    private static final ArrayList<Comment> comments;

    static {
        comments = new ArrayList<>();
    }

    public static ArrayList<Comment> getComments() {
        return comments;
    }


    public static Comment addComment(String text) throws ObjectDoesNotExist {
        Comment comment = new Comment(CustomerController.getCurrentCustomer(), ((BaseProduct) ProductController.getCurrentProduct()).getId()
                , text, CustomerController.isBuyer((BaseProduct) ProductController.getCurrentProduct()));
        comments.add(comment);
        return comment;
    }

}


 class ApplicationController {
    private static final ArrayList<Application> applications;

    static {
        applications = new ArrayList<>();
    }

    public static void addApplication(Object object) {
        applications.add(new Application(CustomerController.getCurrentCustomer(), object));
    }

    public static ArrayList<Application> getApplications() {
        return applications;
    }

    public static Application getById(int id) throws ObjectDoesNotExist {
        for (Application application : applications) {
            if (application.getId() == id) {
                return application;
            }
        }
        throw new ObjectDoesNotExist("Application");
    }
}


 class CartItemController {
    private static final ArrayList<CartItem> cartItems;

    static {
        cartItems = new ArrayList<>();
    }

    public static CartItem addCartItem(Object object, int quantity) throws ObjectDoesNotExist {
        cartItems.add(new CartItem(((BaseProduct) object).getId(), quantity));
        return cartItems.get(cartItems.toArray().length - 1);
    }

    public static ArrayList<CartItem> getCartItems() {
        return cartItems;
    }

    public static CartItem getById(int id) throws ObjectDoesNotExist {
        for (CartItem cartItem : cartItems) {
            if (cartItem.getId() == id) {
                return cartItem;
            }
        }
        throw new ObjectDoesNotExist("point");
    }
}



 class PaymentController {
    private static final ArrayList<Payment> payments;

    static {
        payments = new ArrayList<>();
    }

    public static Payment addPayment(Payment payment) {
        payments.add(payment);
        return payment;
    }

    public static ArrayList<Payment> getPayments() {
        return payments;
    }

}


 class RegexController {
    private final static Pattern emailPattern;
    private final static Pattern cardNumberRegex;
    private final static Pattern cvv2Patter;
    private final static Pattern passwordPattern;
    private final static Pattern phoneNumberPattern;


    static {
        emailPattern = Pattern.compile("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
        cardNumberRegex = Pattern.compile("\\d{4}(-\\d{4}){3}");
        cvv2Patter = Pattern.compile("\\d{4}");
        passwordPattern = Pattern.compile(".{4,}");
        phoneNumberPattern = Pattern.compile("^0(\\d){10}");
    }

    public static boolean checkPassword(String password) {
        Matcher matcher = passwordPattern.matcher(password);
        return matcher.find();
    }

    public static boolean checkPhoneNumber(String phoneNumber) {
        Matcher matcher = phoneNumberPattern.matcher(phoneNumber);
        return matcher.find();
    }

    public static boolean checkEmailRegex(String email) {
        Matcher matcher = emailPattern.matcher(email);
        return matcher.find();
    }

    public static boolean checkCardNumber(String cardNumber) {
        Matcher matcher = cardNumberRegex.matcher(cardNumber);
        return matcher.find();
    }

    public static boolean checkCvv2Pattern(String cvv2) {
        Matcher matcher = cvv2Patter.matcher(cvv2);
        return matcher.find();
    }

}


 class WrongPassWordException extends Exception {
    public WrongPassWordException() {
        super("Wrong password!");
    }
}


 class ObjectDoesNotExist extends Exception {
    public ObjectDoesNotExist(String objectType) {
        super("There is no " + objectType + " with this id");
    }
}


 class CustomerIsNotBuyer extends Exception {
    public CustomerIsNotBuyer() {
        super("This customer has not purchased this product.");
    }
}




 class InsufficientInventory extends Exception {
    public InsufficientInventory() {
        super("Insufficient inventory.");
    }
}


 class CreditNotEnoughException extends Exception {
    public CreditNotEnoughException() {
        super("You don't have enough Credit.");
    }
}


 class Comment {
    private Customer customer;
    private int productId;
    private String text;
    private Condition condition;
    private boolean isBuyer;

    public Comment(Customer customer, int productId, String text, boolean isBuyer) {
        this.customer = customer;
        this.productId = productId;
        this.text = text;
        this.isBuyer = isBuyer;
        this.condition = Condition.AWAITING_CONFORMATION;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public boolean isBuyer() {
        return isBuyer;
    }

    public void setBuyer(boolean buyer) {
        isBuyer = buyer;
    }

    @Override
    public String toString() {
        return "customer=" + customer.getUserName() + "\n" +
                "text=" + text + "\n" +
                "isBuyer=" + isBuyer;
    }
}



 class Point {
    private Customer customer;
    private int point;
    private int productId;
    private Condition condition;

    public Point(Customer customer, int point, int productId) {
        this.customer = customer;
        this.point = point;
        this.productId = productId;
        condition = Condition.AWAITING_CONFORMATION;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getPoint() {
        return point;
    }

    public void setPoint(int point) {
        this.point = point;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }
}


 class Admin extends User {
    private static int lastId;

    static {
        lastId = 0;
    }

    private final int id;
    private long phone;
    private String address;
    private ArrayList<Integer> products;
    private ArrayList<Integer> applications;


    public Admin(String userName, String name, String password, long phone, String address) {
        super(userName, name, password, AccountRole.Admin, password);
        this.phone = phone;
        this.address = address;
        lastId++;
        this.id = lastId;
        this.products = new ArrayList<>();
        this.applications = new ArrayList<>();
    }


    public int getId() {
        return id;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(long phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public ArrayList<Integer> getProducts() {
        return products;
    }

    public void setProducts(ArrayList<Integer> products) {
        this.products = products;
    }

    public ArrayList<Integer> getApplications() {
        return applications;
    }

    public void setApplications(ArrayList<Integer> applications) {
        this.applications = applications;
    }
}


 enum AccountRole {
    Customer,
    Admin
}


 abstract class User {
    private static int lastId;

    static {
        lastId = 0;
    }

    private final int id;
    private String userName;
    private String name;
    private AccountRole accountRole;
    private String password;
    private String email;

    public User(String email, String userName, String name, AccountRole accountRole, String password) {
        lastId++;
        id = lastId;
        this.email = email;
        this.name = name;
        this.accountRole = accountRole;
        this.password = password;
        this.userName = userName;
    }


    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getId() {
        return id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public AccountRole getAccountRole() {
        return accountRole;
    }

    public void setAccountRole(AccountRole accountRole) {
        this.accountRole = accountRole;
    }

    @Override
    public String toString() {
        return "id=" + id + "\n" +
                "userName=" + userName + '\n' +
                "name=" + name + '\n' +
                "password=" + ConsoleColors.RED + "*********" + ConsoleColors.RESET + '\n';
    }
}


 enum ProductCondition {
    AVAILABLE,
    UNAVAILABLE
}


 abstract class BaseProduct {
    private static int lastId;

    static {
        lastId = 0;
    }

    private final int id;
    private int count;
    private String name;
    private int price;
    private ProductCondition productCondition;
    private double averagePoint;
    private Category category;
    private ArrayList<Comment> comments;


    public BaseProduct(String name, int price, ProductCondition productCondition, Category category, int count) {
        lastId++;
        id = lastId;
        this.name = name;
        this.price = price;
        this.productCondition = productCondition;
        this.category = category;
        this.count = count;
    }

//    public BaseProduct(String name, int price, ProductCondition productCondition, Category category) {
//        this(name , price , productCondition , category , 0);
//    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public ProductCondition getProductCondition() {
        return productCondition;
    }

    public void setProductCondition(ProductCondition productCondition) {
        this.productCondition = productCondition;
    }

    public double getAveragePoint() {
        return averagePoint;
    }

    public void setAveragePoint(double averagePoint) {
        this.averagePoint = averagePoint;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public ArrayList<Comment> getComments() {
        return comments;
    }

    public void setComments(ArrayList<Comment> comments) {
        this.comments = comments;
    }


    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void reduceCount(int amount) {
        this.count -= amount;
    }

    @Override
    public String toString() {
        StringBuilder temp = new StringBuilder("⭐️");
        try {
            for (int i = 0; i < ProductController.getPoint(getId()); i++) {
                temp.append("⭐️");
            }
            return "🆔= " + getId() + "\n" +
                    "name=" + name + "\n" +
                    "price=" + price + "＄\n" +
                    "productCondition=" + productCondition + "\n" +
                    "category=" + category + "\n" +
                    temp + "\n";

        } catch (ObjectDoesNotExist e) {
            return e.getMessage();
        }
    }
}


 enum Category {
    DIGITAL,
    STATIONERY,
    VEHICLES,
    FOOD
}


 class Pencil extends BaseStationery {
    private PencilType pencilType;

    public Pencil(String name, int price, ProductCondition productCondition, String producerCountry, PencilType pencilType, int count) {
        super(name, price, productCondition, producerCountry, count);
        this.pencilType = pencilType;
    }


    public PencilType getPencilType() {
        return pencilType;
    }

    public void setPencilType(PencilType pencilType) {
        this.pencilType = pencilType;
    }

    @Override
    public String toString() {
        return super.toString() +
                "pencilType=" + pencilType + "\n";
    }
}


 class BaseStationery extends BaseProduct {

    private String producerCountry;

    public BaseStationery(String name, int price, ProductCondition productCondition, String producerCountry, int count) {
        super(name, price, productCondition, Category.STATIONERY, count);
        this.producerCountry = producerCountry;
    }

    public String getProducerCountry() {
        return producerCountry;
    }

    public void setProducerCountry(String producerCountry) {
        this.producerCountry = producerCountry;
    }

    @Override
    public String toString() {
        return super.toString() +
                "producerCountry='" + producerCountry + "\n";

    }
}


 class NoteBook extends BaseStationery {
    private int pageCount;
    private String pageType;

    public NoteBook(String name, int price, ProductCondition productCondition, String producerCountry, int pageCount, String pageType, int count) {
        super(name, price, productCondition, producerCountry, count);
        this.pageCount = pageCount;
        this.pageType = pageType;
    }


    public int getPageCount() {
        return pageCount;
    }

    public void setPageCount(int pageCount) {
        this.pageCount = pageCount;
    }

    public String getPageType() {
        return pageType;
    }

    public void setPageType(String pageType) {
        this.pageType = pageType;
    }

    @Override
    public String toString() {
        return super.toString() +
                "pageCount=" + pageCount + "\n" +
                "pageType=" + pageType + "\n"
                ;
    }
}


 enum PencilType {
    HH,
    H,
    F,
    B,
    HB
}


 class Pen extends BaseStationery {
    private String color;


    public Pen(String name, int price, ProductCondition productCondition, String producerCountry, String color, int count) {
        super(name, price, productCondition, producerCountry, count);
        this.color = color;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return super.toString() +
                "color=" + color + "\n";
    }
}


 class Bike extends BaseVehicle {
    private BikeType bikeType;

    public Bike(String name, int price, ProductCondition productCondition, String companyName, BikeType bikeType, int count) {
        super(name, price, productCondition, companyName, count);
        this.bikeType = bikeType;
    }


    public BikeType getBikeType() {
        return bikeType;
    }

    public void setBikeType(BikeType bikeType) {
        this.bikeType = bikeType;
    }

    @Override
    public String toString() {
        return super.toString() +
                "bikeType=" + bikeType + "\n";

    }
}


 enum BikeType {
    MOUNTAIN,
    ROAD,
    URBAN,
    HYBRID

}


 class BaseVehicle extends BaseProduct {
    private String companyName;

    public BaseVehicle(String name, int price, ProductCondition productCondition, String companyName, int count) {
        super(name, price, productCondition, Category.VEHICLES, count);
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    @Override
    public String toString() {
        return super.toString() +
                "companyName=" + companyName + "\n";

    }
}


 class Car extends BaseVehicle {
    private int engineCapacity;
    private boolean isAutomatic;

    public Car(String name, int price, ProductCondition productCondition, String producerCountry, int engineCapacity, boolean isAutomatic, int count) {
        super(name, price, productCondition, producerCountry, count);
        this.engineCapacity = engineCapacity;
        this.isAutomatic = isAutomatic;
    }

    public int getEngineCapacity() {
        return engineCapacity;
    }

    public void setEngineCapacity(int engineCapacity) {
        this.engineCapacity = engineCapacity;
    }

    public boolean isAutomatic() {
        return isAutomatic;
    }

    public void setAutomatic(boolean automatic) {
        isAutomatic = automatic;
    }

    @Override
    public String toString() {
        return super.toString() +
                "engineCapacity=" + engineCapacity + "\n" +
                "isAutomatic=" + isAutomatic + "\n"
                ;
    }
}


 class Food extends BaseProduct {
    private String expireDate;
    private String manufactureDate;

    public Food(String name, int price, ProductCondition productCondition, String expireDate, String manufactureDate, int count) {
        super(name, price, productCondition, Category.FOOD, count);
        this.expireDate = expireDate;
        this.manufactureDate = manufactureDate;
    }


    public String getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(String expireDate) {
        this.expireDate = expireDate;
    }

    public String getManufactureDate() {
        return manufactureDate;
    }

    public void setManufactureDate(String manufactureDate) {
        this.manufactureDate = manufactureDate;
    }

    @Override
    public String toString() {
        return super.toString() +
                "expireDate=" + expireDate + "\n" +
                "manufactureDate=" + manufactureDate + "\n";
    }
}


 abstract class BaseDigital extends BaseProduct {
    private int weight;
    private int height;
    private int width;
    private int length;

    public BaseDigital(String name, int price, ProductCondition productCondition, int weight, int height, int width, int length, int count) {
        super(name, price, productCondition, Category.DIGITAL, count);
        this.weight = weight;
        this.height = height;
        this.width = width;
        this.length = length;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    @Override
    public String toString() {
        return super.toString() +
                "weight=" + weight + "\n" +
                "height=" + height + "\n" +
                "width=" + width + "\n" +
                "length=" + length + "\n";

    }
}


 class Pc extends BaseDigital {

    String cpuModel;
    int memoryCapacity;

    public Pc(String name, int price, ProductCondition productCondition, int weight, int height, int width, int length, String cpuModel, int memoryCapacity, int count) {
        super(name, price, productCondition, weight, height, width, length, count);
        this.cpuModel = cpuModel;
        this.memoryCapacity = memoryCapacity;
    }

    public String getCpuModel() {
        return cpuModel;
    }

    public void setCpuModel(String cpuModel) {
        this.cpuModel = cpuModel;
    }

    public int getMemoryCapacity() {
        return memoryCapacity;
    }

    public void setMemoryCapacity(int memoryCapacity) {
        this.memoryCapacity = memoryCapacity;
    }

    @Override
    public String toString() {
        return super.toString() +
                "cpuModel=" + cpuModel + "\n" +
                "memoryCapacity=" + memoryCapacity + "\n";
    }
}


 abstract class BaseDigitalStorage extends BaseDigital {
    private int capacity;

    public BaseDigitalStorage(String name, int price, ProductCondition productCondition, int weight, int height, int width, int length, int capacity, int count) {
        super(name, price, productCondition, weight, height, width, length, count);
        this.capacity = capacity;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    @Override
    public String toString() {
        return super.toString() +
                "capacity=" + capacity + "\n";
    }
}


 class SSD extends BaseDigitalStorage {
    private int writeSpeed, readSpeed;

    public SSD(String name, int price, ProductCondition productCondition, int weight, int height, int width, int length, int capacity, int writeSpeed, int readSpeed, int count) {
        super(name, price, productCondition, weight, height, width, length, capacity, count);
        this.writeSpeed = writeSpeed;
        this.readSpeed = readSpeed;
    }

    public int getWriteSpeed() {
        return writeSpeed;
    }

    public void setWriteSpeed(int writeSpeed) {
        this.writeSpeed = writeSpeed;
    }

    public int getReadSpeed() {
        return readSpeed;
    }

    public void setReadSpeed(int readSpeed) {
        this.readSpeed = readSpeed;
    }

    @Override
    public String toString() {
        return super.toString() +
                "writeSpeed=" + writeSpeed + "\n" +
                "readSpeed=" + readSpeed + "\n";
    }
}


 class USB extends BaseDigitalStorage {
    private double version;

    public USB(String name, int price, ProductCondition productCondition, int weight, int height, int width, int length, int capacity, double version, int count) {
        super(name, price, productCondition, weight, height, width, length, capacity, count);
        this.version = version;
    }


    public double getVersion() {
        return version;
    }

    public void setVersion(double version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return super.toString() +
                "version=" + version + "\n";
    }
}


 enum Condition {
    ACCEPTED,
    AWAITING_CONFORMATION,
    REJECTED
}


 class Customer extends User {
    private long phone;
    private ArrayList<Integer> cart;
    private ArrayList<Integer> history;
    private float credit;
    private String address;

    public Customer(String email, String userName, String password, String name, long phone, String address) {
        super(email, userName, name, AccountRole.Customer, password);
        this.phone = phone;
        this.address = address;
        this.cart = new ArrayList<>();
        this.history = new ArrayList<>();
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public long getPhone() {
        return phone;
    }

    public void setPhone(Long phone) {
        this.phone = phone;
    }

    public ArrayList<Integer> getCart() {
        return cart;
    }

    public void setCart(ArrayList<Integer> cart) {
        this.cart = cart;
    }

    public ArrayList<Integer> getHistory() {
        return history;
    }

    public void setHistory(ArrayList<Integer> history) {
        this.history = history;
    }

    public void addToHistory(Invoice invoice) {
        history.add(invoice.getId());
    }

    public float getCredit() {
        return credit;
    }

    public void setCredit(float credit) {
        this.credit = credit;
    }

    @Override
    public String toString() {

        return super.toString() +
                "phone=" + phone + "\n" +
                "cart=" + cart + "\n" +
                "history=" + history + "\n" +
                "credit=" + credit + "\n" +
                "address='" + address + "\n"
                ;
    }
}


 class CartItem {
    private static int lastId;

    static {
        lastId = 0;
    }

    private final int id;
    private final float unitPrice;
    private int productId;
    private int quantity;

    public CartItem(int productId, int quantity) throws ObjectDoesNotExist {
        lastId++;
        id = lastId;
        this.productId = productId;
        this.quantity = quantity;
        this.unitPrice = ((BaseProduct) ProductController.getProductById(productId)).getPrice();
    }

    public int getId() {
        return id;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getUnitPrice() {
        return unitPrice;
    }

    @Override
    public String toString() {
        String product;
        try {
            product = "[" + ProductController.getProductById(productId).toString() + "]";
        } catch (ObjectDoesNotExist e) {
            throw new RuntimeException(e);
        }
        return "product=\n" + product + "\n" +
                "quantity=" + quantity + "\n";

    }
}


 class Application {
    private static int lastId;

    static {
        lastId = 0;
    }

    private final int id;
    private final Object object;
    private Customer user;
    private Condition condition;

    public Application(Customer customer, Object object) {
        lastId++;
        id = lastId;
        this.user = customer;
        this.condition = Condition.AWAITING_CONFORMATION;
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public int getId() {
        return id;
    }

    public Customer getUser() {
        return user;
    }

    public void setUser(Customer user) {
        this.user = user;
    }

    @Override
    public String toString() {
        return "id=" + id + "\n" +
                "user=" + user.getUserName() + "\n" +
                "object= \n" + ConsoleColors.RED + "{" + object.toString() + "\n}" + ConsoleColors.RESET +
                "condition=" + condition + "\n"
                ;
    }
}


 class Invoice {
    private static int lastId;

    static {
        lastId = 0;
    }

    private final int id;
    private final String date;
    private final ArrayList<Integer> cartItems;
    private float amount;

    public Invoice(ArrayList<Integer> cartItems) throws ObjectDoesNotExist {
        lastId++;
        id = lastId;
        this.date = java.time.LocalDate.now().toString();
        this.cartItems = cartItems;
        this.amount = 0;
        for (Integer cartItem : cartItems) {
            CartItem currentItem = CartItemController.getById(cartItem);
            assert currentItem != null;
            this.amount += currentItem.getQuantity() * currentItem.getUnitPrice();
        }
    }

    public int getId() {
        return id;
    }

    public String getDate() {
        return date;
    }

    public ArrayList<Integer> getCartItems() {
        return cartItems;
    }

    @Override
    public String toString() {
        StringBuilder items = new StringBuilder();
        float amount = 0;
        for (int itemId : cartItems) {
            try {
                CartItem cartItem = CartItemController.getById(itemId);
                items.append(cartItem);
                amount += cartItem.getUnitPrice() * cartItem.getQuantity();
            } catch (ObjectDoesNotExist e) {
                throw new RuntimeException(e);
            }
        }
        return "id=" + id + "\n" +
                "date='" + date + "\n" +
                "items=\n" + items + "\n" +
                "amount=" + amount + "\n"
                ;
    }
}


 class Payment {
    private final String cardNumber;
    private final String CVV2;
    private final String password;
    private final float amount;
    private final Customer customer;
    private Condition condition;

    public Payment(String cardNumber, String CVV2, String password, float amount) {
        this.cardNumber = cardNumber;
        this.CVV2 = CVV2;
        this.password = password;
        this.amount = amount;
        this.customer = CustomerController.getCurrentCustomer();
        this.condition = Condition.AWAITING_CONFORMATION;
    }

    public Condition getCondition() {
        return condition;
    }

    public void setCondition(Condition condition) {
        this.condition = condition;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public String getCVV2() {
        return CVV2;
    }

    public String getPassword() {
        return password;
    }

    public float getAmount() {
        return amount;
    }

    public Customer getCustomer() {
        return customer;
    }

    @Override
    public String toString() {
        return "cardNumber='" + cardNumber + "\n" +
                "CVV2='" + CVV2 + '\n' +
                "password='" + password + '\n' +
                "amount=" + amount + "\n" +
                "customer=" + customer.getUserName() + '\n' +
                "condition=" + condition + '\n';
    }
}


 class Cleaner {
    public static void clear() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    public static void PressEnterToClear() {
        System.out.println(ConsoleColors.RED + "Press enter to continue." + ConsoleColors.RESET);
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
        clear();
    }
}


 class ConsoleColors {
    // Reset
    public static final String RESET = "\033[0m";  // Text Reset

    // Regular Colors
    public static final String BLACK = "\033[0;30m";   // BLACK
    public static final String RED = "\033[0;31m";     // RED
    public static final String GREEN = "\033[0;32m";   // GREEN
    public static final String YELLOW = "\033[0;33m";  // YELLOW
    public static final String BLUE = "\033[0;34m";    // BLUE
    public static final String PURPLE = "\033[0;35m";  // PURPLE
    public static final String CYAN = "\033[0;36m";    // CYAN
    public static final String WHITE = "\033[0;37m";   // WHITE

    // Bold
    public static final String BLACK_BOLD = "\033[1;30m";  // BLACK
    public static final String RED_BOLD = "\033[1;31m";    // RED
    public static final String GREEN_BOLD = "\033[1;32m";  // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String BLUE_BOLD = "\033[1;34m";   // BLUE
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String CYAN_BOLD = "\033[1;36m";   // CYAN
    public static final String WHITE_BOLD = "\033[1;37m";  // WHITE

    // Underline
    public static final String BLACK_UNDERLINED = "\033[4;30m";  // BLACK
    public static final String RED_UNDERLINED = "\033[4;31m";    // RED
    public static final String GREEN_UNDERLINED = "\033[4;32m";  // GREEN
    public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
    public static final String BLUE_UNDERLINED = "\033[4;34m";   // BLUE
    public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
    public static final String CYAN_UNDERLINED = "\033[4;36m";   // CYAN
    public static final String WHITE_UNDERLINED = "\033[4;37m";  // WHITE

    // Background
    public static final String BLACK_BACKGROUND = "\033[40m";  // BLACK
    public static final String RED_BACKGROUND = "\033[41m";    // RED
    public static final String GREEN_BACKGROUND = "\033[42m";  // GREEN
    public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
    public static final String BLUE_BACKGROUND = "\033[44m";   // BLUE
    public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
    public static final String CYAN_BACKGROUND = "\033[46m";   // CYAN
    public static final String WHITE_BACKGROUND = "\033[47m";  // WHITE

    // High Intensity
    public static final String BLACK_BRIGHT = "\033[0;90m";  // BLACK
    public static final String RED_BRIGHT = "\033[0;91m";    // RED
    public static final String GREEN_BRIGHT = "\033[0;92m";  // GREEN
    public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
    public static final String BLUE_BRIGHT = "\033[0;94m";   // BLUE
    public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
    public static final String CYAN_BRIGHT = "\033[0;96m";   // CYAN
    public static final String WHITE_BRIGHT = "\033[0;97m";  // WHITE

    // Bold High Intensity
    public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
    public static final String RED_BOLD_BRIGHT = "\033[1;91m";   // RED
    public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
    public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
    public static final String BLUE_BOLD_BRIGHT = "\033[1;94m";  // BLUE
    public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
    public static final String CYAN_BOLD_BRIGHT = "\033[1;96m";  // CYAN
    public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE

    // High Intensity backgrounds
    public static final String BLACK_BACKGROUND_BRIGHT = "\033[0;100m";// BLACK
    public static final String RED_BACKGROUND_BRIGHT = "\033[0;101m";// RED
    public static final String GREEN_BACKGROUND_BRIGHT = "\033[0;102m";// GREEN
    public static final String YELLOW_BACKGROUND_BRIGHT = "\033[0;103m";// YELLOW
    public static final String BLUE_BACKGROUND_BRIGHT = "\033[0;104m";// BLUE
    public static final String PURPLE_BACKGROUND_BRIGHT = "\033[0;105m"; // PURPLE
    public static final String CYAN_BACKGROUND_BRIGHT = "\033[0;106m";  // CYAN
    public static final String WHITE_BACKGROUND_BRIGHT = "\033[0;107m";   // WHITE

}

 class MainPage extends BaseMenuPage {
    public static void refresh() {
        clear();
        setTitle("Main Menu");
        setMenuItemWidgets(new MenuItemWidget("Sign in", 0)
                , new MenuItemWidget("Log in", 1)
                , new MenuItemWidget("Products", 4));
    }

    public static void start() {
        refresh();
        runMenu();
    }
}


 class SignInPage extends BasePage {

    public static void refresh() {
        clear();
        setTitle("Sign IN");
        setWidgets(new InputWidget("Email"), new InputWidget("Username"), new InputWidget("Password"), new InputWidget("name"),
                new InputWidget("Phone number"), new InputWidget("address"));
    }

    public static void runSignIn() {
        refresh();
        SignInPage.run();
        String email = getWidgets().get(0).getObject().toString();
        String password = getWidgets().get(2).getObject().toString();
        String phoneNumber = getWidgets().get(4).getObject().toString();
        if (RegexController.checkEmailRegex(email)) {
            if (RegexController.checkPassword(password)) {
                if (RegexController.checkPhoneNumber(phoneNumber)) {
                    Customer customer = new Customer(
                            getWidgets().get(0).getObject().toString(),
                            getWidgets().get(1).getObject().toString()
                            , getWidgets().get(2).getObject().toString()
                            , getWidgets().get(3).getObject().toString()
                            , Long.parseLong(getWidgets().get(4).getObject().toString())
                            , getWidgets().get(5).getObject().toString());
                    ApplicationController.addApplication(customer);
                    System.out.println("You sign in Application has been sent");
                } else {
                    System.out.println("Wrong phone number format.");
                }
            } else {
                System.out.println("password should have at least 4 character.");
            }
        } else {
            System.out.println("Wrong email Format.");
        }
        Cleaner.PressEnterToClear();
        Router.rout(-1);


    }
}


 class LoginPage extends BasePage {

    public static void refresh() {
        clear();
        setTitle("Login");
        setWidgets(new InputWidget("Username"), new InputWidget("Password"));
    }

    public static void runLogin() {
        refresh();
        LoginPage.run();
        try {
            Customer customerTemp = CustomerController.getByUserName(getWidgets().get(0).getObject().toString());
            try {
                CustomerController.login(customerTemp, getWidgets().get(1).getObject().toString());
                System.out.println("you are logged in");
                Cleaner.PressEnterToClear();
                Router.rout(3);

            } catch (WrongPassWordException W) {
                System.out.println("Wrong password!!");
                Cleaner.PressEnterToClear();
                runLogin();
            } finally {
                System.out.println("done.");
            }
        } catch (ObjectDoesNotExist o) {
            if (getWidgets().get(0).getObject().toString().equals("admin")) {
                if (getWidgets().get(1).getObject().toString().equals("admin")) {
                    Router.rout(10);
                }
            }
            System.out.println("There is no user With this Username");
            Cleaner.PressEnterToClear();
            runLogin();
        }
    }
}


 class PaymentPage extends BasePage {
    public static void refresh() {
        clear();
        setTitle("Payment");
        setWidgets(
                new InputWidget("Card Number"),
                new InputWidget("CVV2"),
                new InputWidget("Password"),
                new InputWidget("Amount")
        );
    }

    public static void start() {
        Cleaner.clear();
        refresh();
        run();
        if (RegexController.checkCardNumber(getWidgets().get(0).getObject().toString())) {
            if (RegexController.checkCvv2Pattern(getWidgets().get(1).getObject().toString())) {
                ApplicationController.addApplication(PaymentController.addPayment(new Payment(
                        getWidgets().get(0).getObject().toString(),
                        getWidgets().get(1).getObject().toString(),
                        getWidgets().get(2).getObject().toString(),
                        Float.parseFloat(getWidgets().get(3).getObject().toString())
                )));
                System.out.println("Your Application has been sent to admin.");
                Cleaner.PressEnterToClear();
                Router.rout(-1);
            } else {
                System.out.println("Wrong CVV2.");
                Cleaner.PressEnterToClear();
                Router.rout(-1);
            }
        } else {
            System.out.println("Wrong card number.");
            Cleaner.PressEnterToClear();
            Router.rout(-1);
        }

    }
}


 class AdminPage extends BaseMenuPage {
    private static final String[] helps = {
            "|------------------------------------------------------|",
            "|PRODUCTS                                              |",
            "|--------|LIST     : List of products                  |",
            "|------------------------------------------------------|",
            "|ADD : add product.                                    |",
            "|------------------------------------------------------|",
            "|--------|CAR      : add Car product.                  |",
            "|--------|Bike     : add Bike product.                 |",
            "|------------------------------------------------------|",
            "|--------|NOTEBOOK : add notebook product.             |",
            "|--------|PEN      : add pen product.                  |",
            "|--------|PENCIL   : add pencil product.               |",
            "|------------------------------------------------------|",
            "|--------|FOOD     : add food product.                 |",
            "|------------------------------------------------------|",
            "|--------|PC      : add PC product.                    |",
            "|--------|SSD     : add SSD product.                   |",
            "|--------|USB     : add USB product.                   |",
            "|EDIT : edit product.                                  |",
            "|--------|{PRODUCT_ID}                                 |",
            "|------------------------------------------------------|",
            "|REMOVE : remove product.                              |",
            "|--------|{PRODUCT_ID}                                 |",
            "|------------------------------------------------------|",
            "|APPLICATIONS                                          |",
            "|--------|LIST     : List of applications.             |",
            "|--------|ACCEPT   : Accept an application.            |",
            "|--------|REJECT   : Reject an application.            |",
            "|------------------------------------------------------|",
            "|CUSTOMERS                                             |",
            "|--------|LIST     : List of customers.                |",
            "|------------------------------------------------------|",
            "|EXIT              :back to main menu                  |",
            "|------------------------------------------------------|",


    };

    public static void refresh() {
        clear();
        setTitle("");
        setWidgets(new InputWidget(ConsoleColors.BLUE + "➜ " + ConsoleColors.RED_BOLD + "(ADMIN) " + ConsoleColors.YELLOW + "~ " + ConsoleColors.RESET));
    }

    public static void help() {

    }

    public static void start() {
        refresh();
        run();
        ArrayList<String> commands = new ArrayList<>(List.of(getWidgets().get(0).getObject().toString().split(" ")));
        switch (commands.get(0)) {
            case "HELP" -> {
                for (String s : helps) {
                    System.out.println(s);
                }
            }
            case "PRODUCTS" -> {
                try {
                    if (commands.get(1) == "LIST") {
                        for (Object product : ProductController.getProducts()) {
                            System.out.println(product.toString());
                        }
                    }

                } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    System.out.println(helps[0]);
                    System.out.println(helps[1]);
                    System.out.println(helps[2]);
                    System.out.println(helps[3]);
                }
            }
            case "EXIT" -> Router.rout(-1);
            case "ADD" -> {
                try {
                    switch (commands.get(1)) {
                        case "CAR" -> {
                            ProductController.addProduct(
                                    new Car(
                                            commands.get(2)
                                            ,
                                            Integer.parseInt(commands.get(3))
                                            ,
                                            ProductCondition.AVAILABLE
                                            ,
                                            commands.get(4)
                                            ,
                                            Integer.parseInt(commands.get(5))
                                            ,
                                            Boolean.parseBoolean(commands.get(6).toLowerCase())
                                            ,
                                            Integer.parseInt(commands.get(7))
                                    )
                            );
                            System.out.println("Product ADDED");
                        }
                        case "BIKE" -> {
                            ProductController.addProduct(
                                    new Bike(
                                            commands.get(2)
                                            ,
                                            Integer.parseInt(commands.get(3))
                                            ,
                                            ProductCondition.AVAILABLE
                                            ,
                                            commands.get(4)
                                            ,
                                            BikeType.valueOf(commands.get(5))
                                            ,
                                            Integer.parseInt(commands.get(6))
                                    )
                            );
                            System.out.println("Product ADDED");
                        }
                        case "NOTEBOOK" -> {
                            ProductController.addProduct(
                                    new NoteBook(
                                            commands.get(2)
                                            ,
                                            Integer.parseInt(commands.get(3))
                                            ,
                                            ProductCondition.AVAILABLE
                                            ,
                                            commands.get(4)
                                            ,
                                            Integer.parseInt(commands.get(5))
                                            ,
                                            commands.get(6)
                                            ,
                                            Integer.parseInt(commands.get(7))
                                    )
                            );
                            System.out.println("Product ADDED");
                        }
                        case "PEN" -> {
                            ProductController.addProduct(
                                    new Pen(
                                            commands.get(2),
                                            Integer.parseInt(commands.get(3)),
                                            ProductCondition.AVAILABLE,
                                            commands.get(4),
                                            commands.get(5),
                                            Integer.parseInt(commands.get(6))
                                    )
                            );
                            System.out.println("Product Added");
                        }
                        case "PENCIL" -> {
                            ProductController.addProduct(
                                    new Pencil(
                                            commands.get(2),
                                            Integer.parseInt(commands.get(3)),
                                            ProductCondition.AVAILABLE,
                                            commands.get(4),
                                            PencilType.valueOf(commands.get(5)),
                                            Integer.parseInt(commands.get(6))
                                    )
                            );
                            System.out.println("Product Added");
                        }
                        case "FOOD" -> {
                            ProductController.addProduct(
                                    new Food(
                                            commands.get(2),
                                            Integer.parseInt(commands.get(3)),
                                            ProductCondition.AVAILABLE,
                                            commands.get(4),
                                            commands.get(5),
                                            Integer.parseInt(commands.get(6))
                                    )
                            );
                            System.out.println("Product ADDED");
                        }
                        case "PC" -> {
                            ProductController.addProduct(
                                    new Pc(
                                            commands.get(2),
                                            Integer.parseInt(commands.get(3)),
                                            ProductCondition.AVAILABLE,
                                            Integer.parseInt(commands.get(4)),
                                            Integer.parseInt(commands.get(5)),
                                            Integer.parseInt(commands.get(6)),
                                            Integer.parseInt(commands.get(7)),
                                            commands.get(8),
                                            Integer.parseInt(commands.get(9)),
                                            Integer.parseInt(commands.get(10))
                                    )

                            );
                            System.out.println("Product added");
                        }
                        case "SSD" -> {
                            ProductController.addProduct(
                                    new SSD(
                                            commands.get(2),
                                            Integer.parseInt(commands.get(3)),
                                            ProductCondition.AVAILABLE,
                                            Integer.parseInt(commands.get(4)),
                                            Integer.parseInt(commands.get(5)),
                                            Integer.parseInt(commands.get(6)),
                                            Integer.parseInt(commands.get(7)),
                                            Integer.parseInt(commands.get(8)),
                                            Integer.parseInt(commands.get(9)),
                                            Integer.parseInt(commands.get(10)),
                                            Integer.parseInt(commands.get(11))
                                    )
                            );
                            System.out.println("Product added");
                        }
                        case "USB" -> {
                            ProductController.addProduct(
                                    new USB(
                                            commands.get(2),
                                            Integer.parseInt(commands.get(3)),
                                            ProductCondition.AVAILABLE,
                                            Integer.parseInt(commands.get(4)),
                                            Integer.parseInt(commands.get(5)),
                                            Integer.parseInt(commands.get(6)),
                                            Integer.parseInt(commands.get(7)),
                                            Integer.parseInt(commands.get(8)),
                                            Double.parseDouble(commands.get(9)),
                                            Integer.parseInt(commands.get(10))
                                    )
                            );
                            System.out.println("Product added");
                        }
                    }
                } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    for (String s : Arrays.copyOfRange(helps, 5, 19)) {
                        System.out.println(s);
                    }
                }
            }
            case "EDIT" -> {
                try {
                    Object object = ProductController.getProductById(Integer.parseInt(commands.get(1)));
                    switch (commands.get(2)) {
                        case "NAME" -> ((BaseProduct) object).setName(commands.get(3));
                        case "PRICE" -> ((BaseProduct) object).setPrice(Integer.parseInt(commands.get(3)));
                        case "COUNT" -> ((BaseProduct) object).setCount(Integer.parseInt(commands.get(3)));
                    }
                    System.out.println("Product edited.");
                } catch (ObjectDoesNotExist | IndexOutOfBoundsException o) {
                    for (String s : Arrays.copyOfRange(helps, 27, 31)) {
                        System.out.println(s);
                    }
                }
            }
            case "REMOVE" -> {
                try {
                    ProductController.removeProduct(ProductController.getProductById(Integer.parseInt(commands.get(1))));
                } catch (ObjectDoesNotExist o) {
                    System.out.println("There is no product with this id");
                } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    for (String s : Arrays.copyOfRange(helps, 20, 24)) {
                        System.out.println(s);
                    }
                }
            }
            case "APPLICATIONS" -> {
                try {
                    switch (commands.get(1)) {
                        case "LIST" -> {
                            for (Application application : ApplicationController.getApplications()) {
                                System.out.println(
                                        ConsoleColors.YELLOW +
                                                "---------------------------------------" +
                                                ConsoleColors.RESET);
                                System.out.println(application.toString());
                            }

                        }
                        case "ACCEPT" -> {
                            try {
                                AdminController.acceptApplication(Integer.parseInt(commands.get(2)));
                                System.out.println("Application accepted 🤝");
                            } catch (ObjectDoesNotExist e) {
                                System.out.println(e.getMessage());
                            }
                        }
                        case "REJECT" -> {
                            try {
                                AdminController.rejectApplication(Integer.parseInt(commands.get(2)));
                                System.out.println("Application rejected ✋🏻");
                            } catch (ObjectDoesNotExist e) {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
                } catch (IndexOutOfBoundsException indexOutOfBoundsException) {
                    for (String s : Arrays.copyOfRange(helps, 24, 29)) {
                        System.out.println(s);
                    }
                }

            }
            case "CUSTOMERS" -> {
                for (Customer customer : CustomerController.getCustomers()) {
                    System.out.println(
                            ConsoleColors.YELLOW +
                                    "---------------------------------------" +
                                    ConsoleColors.RESET);

                    System.out.println(ConsoleColors.BLUE + customer.toString() + ConsoleColors.RESET);
                }
            }
        }
        Cleaner.PressEnterToClear();
        start();
    }
}


 class ProductDetail {
    public static void start() {
        System.out.print(ConsoleColors.BLUE);
        System.out.println(ProductController.getCurrentProduct().toString());
        System.out.print(ConsoleColors.RESET);
        System.out.println(ConsoleColors.YELLOW + "Comments ---------------------------------------" + ConsoleColors.GREEN);
        for (Comment comment : CommentController.getComments()) {
            if (comment.getProductId() == ((BaseProduct) ProductController.getCurrentProduct()).getId() && comment.getCondition().equals(Condition.ACCEPTED)) {
                System.out.println(ConsoleColors.BLUE);
                System.out.println(comment);
                System.out.println(ConsoleColors.YELLOW);
                System.out.println("|---------");
                System.out.println(ConsoleColors.RESET);
            }
        }


        System.out.print(ConsoleColors.YELLOW + "(1 : add to cart , 2 : add comment , 3 : give a point to product , 4: back ) ➡️ : " + ConsoleColors.RESET);
        Scanner scanner = new Scanner(System.in);
        int choice = scanner.nextInt();
        if (CustomerController.getCurrentCustomer() == null) {
            System.out.println(ConsoleColors.RED + "You are not logged in you will redirect to login page" + ConsoleColors.RESET);
            Cleaner.PressEnterToClear();
            Router.rout(1);

        } else {
            switch (choice) {
                case 1 -> {
                    Cleaner.clear();
                    System.out.print("\nQuantity : ");
                    int quantity = scanner.nextInt();
                    try {
                        CustomerController.addToCart((BaseProduct) ProductController.getCurrentProduct(), quantity);
                        System.out.println("Done");
                        Cleaner.PressEnterToClear();
                    } catch (ObjectDoesNotExist | InsufficientInventory o) {
                        System.out.println(o.getMessage());
                    }
                    Router.rout(9);
                }
                case 2 -> {
                    Cleaner.PressEnterToClear();
                    System.out.print("\nComment 💬 : ");
                    scanner.nextLine();
                    String comment = scanner.nextLine();
                    System.out.println(comment);
                    try {
                        ApplicationController.addApplication(CommentController.addComment(comment));
                        System.out.println("Done");
                        Cleaner.PressEnterToClear();
                        Router.rout(9);
                    } catch (ObjectDoesNotExist o) {
                    }
                }
                case 3 -> {
                    Cleaner.clear();
                    System.out.print("Point (0 - 10) ⭐️ : ");
                    int point = scanner.nextInt();
                    try {
                        if (CustomerController.isBuyer((BaseProduct) ProductController.getCurrentProduct())) {
                            ApplicationController.addApplication(PointController.addPoint(point));
                            System.out.println("point added successfully");
                            Cleaner.PressEnterToClear();
                        } else {
                            System.out.println(ConsoleColors.RED + "You are not the buyer of this product!💥" + ConsoleColors.RESET);
                            Cleaner.PressEnterToClear();
                        }


                    } catch (ObjectDoesNotExist objectDoesNotExist) {
                    }
                    Router.rout(9);
                }
                case 4 -> {
                    Router.rout(-1);
                }

            }
        }
    }
}


 class ProductPage extends BaseMenuPage {
    private static void refresh() {
        clear();
        setBeforeItem(ConsoleColors.YELLOW + "------------------------------\n" + ConsoleColors.RESET);
        setAfterItem("");
        setTitle("Product List");
        for (Object product : ProductController.getProducts()) {
            addMenuItem(new ProductMenuItem("\n" + product.toString(), product));
        }
    }

    public static void start() {
        refresh();
        runMenu();
    }
}


 abstract class BasePage {
    private static String title;
    private static ArrayList<BaseWidget> widgets;
    private static int previousPage;

    static {
        widgets = new ArrayList<>();
    }

    public static void addWidget(BaseWidget baseWidget) {
        BasePage.widgets.add(baseWidget);
    }

    public static int getPreviousPage() {
        return previousPage;
    }

    public static void setPreviousPage(int previousPage) {
        BasePage.previousPage = previousPage;
    }

    protected static void run() {
        Cleaner.clear();
        System.out.println(ConsoleColors.YELLOW + "-------" + title + "-------" + ConsoleColors.RESET);
        for (BaseWidget widget : widgets) {
            widget.run();
        }
    }

    public static String getTitle() {
        return title;
    }

    public static void setTitle(String title) {
        BasePage.title = title;
    }

    protected static void clear() {
        setPreviousPage(-1);
        setTitle("");
        widgets = new ArrayList<>();
    }

    public static ArrayList<BaseWidget> getWidgets() {
        return widgets;
    }

    public static void setWidgets(BaseWidget... widgets) {
        BasePage.widgets.clear();
        BasePage.widgets.addAll(List.of(widgets));
    }
}


 abstract class BaseMenuPage extends BasePage {
    private static final Scanner scanner;
    private static String header;
    private static String footer;
    private static String beforeItem;
    private static String afterItem;
    private static ArrayList<MenuItemWidget> menuItemWidgets;

    static {
        scanner = new Scanner(System.in);
        menuItemWidgets = new ArrayList<>();
        header = "";
        footer = "";
        beforeItem = "";
        afterItem = "\t\t\t\t";
    }

    public static void setHeader(String header) {
        BaseMenuPage.header = header;
    }

    public static void setFooter(String footer) {
        BaseMenuPage.footer = footer;
    }

    public static void setBeforeItem(String beforeItem) {
        BaseMenuPage.beforeItem = beforeItem;
    }

    public static void setAfterItem(String afterItem) {
        BaseMenuPage.afterItem = afterItem;
    }

    public static void addMenuItem(MenuItemWidget menuItemWidget) {
        menuItemWidgets.add(menuItemWidget);
    }

    public static ArrayList<MenuItemWidget> getMenuItemWidgets() {
        return menuItemWidgets;
    }

    public static void setMenuItemWidgets(MenuItemWidget... menuItemWidgets) {
        BaseMenuPage.menuItemWidgets.addAll(List.of(menuItemWidgets));
    }

    public static void runMenu(boolean getInput) {

        if ((Router.getHistory().toArray().length > 1)) {
            addMenuItem(new MenuItemWidget("Back", -1));
        }
        int index = 0;
        System.out.println(ConsoleColors.YELLOW + "-------------- " + getTitle() + " --------------" + ConsoleColors.RESET);
        if (!header.equals("")) System.out.println(header);
        for (MenuItemWidget menuItemWidget : getMenuItemWidgets()) {
            System.out.print(beforeItem);
            if (index % 2 == 0 && index != 0) {
                System.out.print('\n');
            }
            index++;
            System.out.print(ConsoleColors.RED + index + ConsoleColors.RESET + " ");
            System.out.print(ConsoleColors.BLUE);
            menuItemWidget.run();
            System.out.print(ConsoleColors.RESET);

            if (menuItemWidget.getTitle().length() > 16) {
                System.out.print("\t");
            } else {
                System.out.print(afterItem);
            }

        }
        if (!footer.equals("")) System.out.println(footer);
        index = 0;

    }

    private static void refresh() {
    }

    public static void runMenu() {
        runMenu(true);
        getInput();

    }

    public static void getInput() {
        System.out.println(ConsoleColors.YELLOW + "\nYour choice ➡️ : " + ConsoleColors.RESET);
        int selected = scanner.nextInt();
        (menuItemWidgets.get(selected - 1)).select();
    }


    public static void clear() {
        setPreviousPage(-1);
        setTitle("");
        menuItemWidgets = new ArrayList<>();
        getWidgets().clear();
        setFooter("");
        setHeader("");
        setAfterItem("\t\t");
        setBeforeItem("");

    }


}


 class InvoicePage extends BaseMenuPage {
    public static void refresh() {
        clear();
        for (Invoice invoice : InvoiceController.getInvoices()) {
            addMenuItem(new MenuItemWidget(invoice.toString()));
        }
    }


    public static void start() {
        Cleaner.clear();
        refresh();
        runMenu(false);
        Cleaner.PressEnterToClear();
        Router.rout(-1);
    }
}


 class ChangeInfo extends BaseMenuPage {
    private static Customer customer;

    private static void refresh() {
        clear();
        setTitle("Change info");
        customer = CustomerController.getCurrentCustomer();
        setMenuItemWidgets(
                new MenuItemWidget("UserName : " + customer.getUserName()),
                new MenuItemWidget("password : " + "*********"),
                new MenuItemWidget("name : " + customer.getName()),
                new MenuItemWidget("Phone : " + customer.getPhone()),
                new MenuItemWidget("address : " + customer.getAddress()),
                new MenuItemWidget("email : " + customer.getEmail())
        );
    }

    public static void changeInfoRun() {
        refresh();
        ChangeInfo.runMenu(true);
        Scanner scanner = new Scanner(System.in);
        System.out.print("\nSelect one to change : ");
        int userChoice = scanner.nextInt();
        Cleaner.clear();
        InputWidget inputWidget = new InputWidget("");
        switch (userChoice) {
            case 1 -> {
                inputWidget.setMessage("UserName");
                inputWidget.run();
                customer.setUserName(inputWidget.getObject().toString());
            }
            case 2 -> {
                inputWidget.setMessage("Password");
                inputWidget.run();
                customer.setPassword(inputWidget.getObject().toString());
            }
            case 3 -> {
                inputWidget.setMessage("Name");
                inputWidget.run();
                customer.setName(inputWidget.getObject().toString());
            }
            case 4 -> {
                inputWidget.setMessage("Phone");
                inputWidget.run();
                customer.setPhone(Long.parseLong(inputWidget.getObject().toString()));
            }
            case 5 -> {
                inputWidget.setMessage("Address");
                inputWidget.run();
                customer.setAddress(inputWidget.getObject().toString());
            }
            case 6 -> {
                inputWidget.setMessage("Email");
                inputWidget.run();
                customer.setEmail(inputWidget.getObject().toString());
            }
            case 7 -> {
                Router.rout(-1);
            }
        }
        CustomerController.changePersonalInfo(customer);
        CustomerController.setCurrentCustomer(CustomerController.getCustomers().get(CustomerController.getCustomers().indexOf(customer)));
        refresh();
        Cleaner.clear();
        System.out.println("Done.");
        Cleaner.PressEnterToClear();
        ChangeInfo.changeInfoRun();
    }


}


 class CustomerPage extends BaseMenuPage {

    public static void start() {
        clear();
        setTitle("Customer Dashboard");
        setAfterItem("\t\t\t\t");
        setMenuItemWidgets(
                new MenuItemWidget("Edit Personal info", 2),
                new MenuItemWidget("Cart", 5),
                new MenuItemWidget("Products", 4),
                new MenuItemWidget("Add Credit to account", 6),
                new MenuItemWidget("History", 7)
        );
        CustomerPage.runMenu();
    }


}


 class CartPage extends BaseMenuPage {


    private static void refresh() {
        clear();
        setTitle("Cart Page " + " Your credits : " + CustomerController.getCurrentCustomer().getCredit());
        try {
            for (CartItem cartItem : CustomerController.cart()) {
                addMenuItem(new MenuItemWidget(cartItem.toString()));
            }

        } catch (ObjectDoesNotExist o) {
            System.out.println(o.getMessage());
        }
    }


    public static void start() {
        refresh();
        runMenu(false);
        Scanner scanner = new Scanner(System.in);
        System.out.print("Press 1 for payment and -1 for back :  ");
        int choice = scanner.nextInt();
        if (choice == 1) {
            try {
                CustomerController.buy();
                Cleaner.clear();
                System.out.println("Payment was successful.");
            } catch (CreditNotEnoughException e) {
                System.out.println(e.getMessage());
            } catch (ObjectDoesNotExist e) {
                System.out.println(e.getMessage());
            }
            Cleaner.PressEnterToClear();
            Router.rout(-1);

        } else if (choice == -1) {
            Router.rout(-1);
        }

    }

}


 class ProductMenuItem extends MenuItemWidget {
    private final Object object;

    public ProductMenuItem(String title, Object object) {
        super(title, 9);
        this.object = object;

    }

    @Override
    public void select() {
        ProductController.setCurrentProduct(object);
        super.select();

    }
}


 class InputWidget extends BaseWidget {
    private final static Scanner scanner;

    static {
        scanner = new Scanner(System.in);
    }

    private String message = "Empty message";

    public InputWidget(String message) {
        this.message = message;
    }


    public String getMessage() {
        return message;
    }

    public void setMessage(String m) {
        message = m;
    }

    @Override
    public void run() {
        Cleaner.clear();
        System.out.print(ConsoleColors.YELLOW + message + " : ");
        setObject(scanner.nextLine());
        System.out.print(ConsoleColors.RESET);
    }
}



 abstract class BaseWidget {
    private static int lastId;

    static {
        lastId = 0;
    }

    private final int id;
    private Object object;

    public BaseWidget() {
        lastId++;
        id = lastId;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public abstract void run();

    public void rout() {
    }


    public int getId() {
        return id;
    }
}


 class MenuItemWidget extends BaseWidget {
    private final String title;
    private final int linkedPage;


    public MenuItemWidget(String title, int linkedPage) {
        super();
        this.linkedPage = linkedPage;
        this.title = title;

    }

    public MenuItemWidget(String title) {
        this(title, -1);
    }

    public String getTitle() {
        return title;
    }

    public int getLinkedPage() {
        return linkedPage;
    }

    @Override
    public void run() {
        System.out.print(title);
    }

    public void select() {
        Router.rout(linkedPage);
    }
}


 class Router {
    private static int currentPage;
    private static final ArrayList<Integer> history;

    static {
        history = new ArrayList<>();
        currentPage = -1;
    }

    public static ArrayList<Integer> getHistory() {
        return history;
    }

    private static void selectPage(int pageId) {

        switch (pageId) {
            case -1 -> back();
            case 0 -> SignInPage.runSignIn();
            case 1 -> LoginPage.runLogin();
            case 2 -> ChangeInfo.changeInfoRun();
            case 3 -> CustomerPage.start();
            case 4 -> ProductPage.start();
            case 5 -> CartPage.start();
            case 6 -> PaymentPage.start();
            case 7 -> InvoicePage.start();
            case 8 -> MainPage.start();
            case 9 -> ProductDetail.start();
            case 10 -> AdminPage.start();

        }
    }

    public static void rout(int pageId) {
        Cleaner.clear();
        if (currentPage != pageId && pageId != -1 && pageId != 1) {
            currentPage = pageId;
            history.add(pageId);
        }
        selectPage(pageId);
    }

    public static void back() {
        history.remove(history.toArray().length - 1);
        currentPage = history.get(history.toArray().length - 1);
        rout(history.get(history.toArray().length - 1));
    }


}
