import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

// =================================================================================
// 1. DATA MODEL CLASSES
// =================================================================================

/**
 * Represents a single product in the catalog.
 */
class Product {
    private final int id;
    private final String name;
    private final String description;
    private final double price;

    public Product(int id, String name, String description, double price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public double getPrice() { return price; }
}

/**
 * Represents an item added to the shopping cart.
 */
class CartItem {
    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() { return product; }
    public int getQuantity() { return quantity; }
    
    public void addQuantity(int amount) { this.quantity += amount; }
    public double getSubtotal() { return product.getPrice() * quantity; }
}

// =================================================================================
// 2. CORE LOGIC CLASS (Backend & State Management)
// =================================================================================

class EcommerceStore {
    private final List<Product> catalog = new ArrayList<>();
    private final List<CartItem> shoppingCart = new ArrayList<>();

    public EcommerceStore() {
        // Pre-load the catalog with dummy data for testing
        catalog.add(new Product(101, "Wireless Headphones", "Noise-cancelling over-ear headphones.", 199.99));
        catalog.add(new Product(102, "Mechanical Keyboard", "RGB backlit keyboard with blue switches.", 89.50));
        catalog.add(new Product(103, "Gaming Mouse", "High DPI ergonomic mouse.", 45.00));
        catalog.add(new Product(104, "27-inch Monitor", "4K Ultra HD IPS display.", 349.99));
        catalog.add(new Product(105, "USB-C Hub", "7-in-1 multi-port adapter.", 25.00));
    }

    // --- Catalog Management ---
    public List<Product> getCatalog() {
        return catalog;
    }

    public Product findProductById(int id) {
        return catalog.stream().filter(p -> p.getId() == id).findFirst().orElse(null);
    }

    // --- Cart Management ---
    public String addToCart(int productId, int quantity) {
        if (quantity <= 0) return "Quantity must be at least 1.";
        
        Product product = findProductById(productId);
        if (product == null) return "Error: Product not found.";

        // Check if item is already in cart, if so just update quantity
        for (CartItem item : shoppingCart) {
            if (item.getProduct().getId() == productId) {
                item.addQuantity(quantity);
                return "Updated quantity of " + product.getName() + " in cart.";
            }
        }

        // Otherwise add new item
        shoppingCart.add(new CartItem(product, quantity));
        return "Added " + product.getName() + " to cart!";
    }

    public List<CartItem> getCartItems() {
        return shoppingCart;
    }

    public double calculateTotal() {
        return shoppingCart.stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public void clearCart() {
        shoppingCart.clear();
    }

    // --- Payment Simulation ---
    public boolean processPayment(String cardNumber, String expiry) {
        // Basic simulation: Accept any card that is 16 digits long
        if (cardNumber != null && cardNumber.replaceAll("\\s", "").length() == 16) {
            clearCart();
            return true; // Payment success
        }
        return false; // Payment failed
    }
}

// =================================================================================
// 3. MAIN GUI AND APPLICATION CLASS
// =================================================================================

public class EcommerceGUI extends JFrame {
    private final EcommerceStore store;
    
    // UI Components
    private final DefaultTableModel catalogTableModel;
    private final DefaultTableModel cartTableModel;
    private final JLabel totalLabel;

    public EcommerceGUI() {
        this.store = new EcommerceStore();

        // --- Frame Setup ---
        setTitle("Java E-Commerce Platform");
        setSize(850, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // --- Models for Tables ---
        catalogTableModel = new DefaultTableModel(new String[]{"ID", "Product Name", "Description", "Price ($)"}, 0);
        cartTableModel = new DefaultTableModel(new String[]{"Product Name", "Price", "Quantity", "Subtotal ($)"}, 0);
        totalLabel = new JLabel("Total: $0.00");
        totalLabel.setFont(new Font("Arial", Font.BOLD, 18));

        // --- Tabbed Pane ---
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Product Catalog", createCatalogPanel());
        tabbedPane.addTab("Shopping Cart", createCartPanel());

        // Update cart tab whenever user switches to it
        tabbedPane.addChangeListener(e -> {
            if (tabbedPane.getSelectedIndex() == 1) {
                refreshCartTable();
            }
        });

        getContentPane().add(tabbedPane);
        refreshCatalogTable();
    }

    // --- Panel: Product Catalog ---
    private JPanel createCatalogPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table View
        JTable catalogTable = new JTable(catalogTableModel);
        catalogTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JScrollPane scrollPane = new JScrollPane(catalogTable);

        // Add to Cart Form
        JPanel formPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 10));
        JTextField productIdField = new JTextField(8);
        JTextField quantityField = new JTextField("1", 5);
        JButton addButton = new JButton("Add to Cart");

        formPanel.add(new JLabel("Product ID:"));
        formPanel.add(productIdField);
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(quantityField);
        formPanel.add(addButton);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(formPanel, BorderLayout.SOUTH);

        // Convenience: Auto-fill Product ID when a row is clicked
        catalogTable.getSelectionModel().addListSelectionListener(e -> {
            int selectedRow = catalogTable.getSelectedRow();
            if (selectedRow >= 0) {
                productIdField.setText(catalogTableModel.getValueAt(selectedRow, 0).toString());
            }
        });

        // Add Button Logic
        addButton.addActionListener(e -> {
            try {
                int id = Integer.parseInt(productIdField.getText().trim());
                int qty = Integer.parseInt(quantityField.getText().trim());
                
                String result = store.addToCart(id, qty);
                JOptionPane.showMessageDialog(this, result, "Cart Update", JOptionPane.INFORMATION_MESSAGE);
                
                // Reset inputs
                productIdField.setText("");
                quantityField.setText("1");
                catalogTable.clearSelection();
                
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter valid numeric values for ID and Quantity.", "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        return panel;
    }

    // --- Panel: Shopping Cart & Checkout ---
    private JPanel createCartPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Table View
        JTable cartTable = new JTable(cartTableModel);
        JScrollPane scrollPane = new JScrollPane(cartTable);

        // Checkout Section
        JPanel bottomPanel = new JPanel(new BorderLayout());
        JButton checkoutButton = new JButton("Proceed to Checkout");
        checkoutButton.setFont(new Font("Arial", Font.BOLD, 14));
        checkoutButton.setBackground(new Color(34, 139, 34)); // Forest Green
        checkoutButton.setForeground(Color.WHITE);

        bottomPanel.add(totalLabel, BorderLayout.WEST);
        bottomPanel.add(checkoutButton, BorderLayout.EAST);

        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(bottomPanel, BorderLayout.SOUTH);

        // Checkout Button Logic
        checkoutButton.addActionListener(e -> {
            if (store.getCartItems().isEmpty()) {
                JOptionPane.showMessageDialog(this, "Your cart is empty!", "Checkout Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            // Simulate Payment Dialog
            JPanel paymentPanel = new JPanel(new GridLayout(2, 2, 5, 5));
            JTextField cardField = new JTextField();
            JTextField expiryField = new JTextField();
            
            paymentPanel.add(new JLabel("Card Number (16 digits):"));
            paymentPanel.add(cardField);
            paymentPanel.add(new JLabel("Expiry (MM/YY):"));
            paymentPanel.add(expiryField);

            int result = JOptionPane.showConfirmDialog(this, paymentPanel, "Payment Gateway - Total: $" + String.format("%.2f", store.calculateTotal()), JOptionPane.OK_CANCEL_OPTION);
            
            if (result == JOptionPane.OK_OPTION) {
                boolean success = store.processPayment(cardField.getText(), expiryField.getText());
                if (success) {
                    JOptionPane.showMessageDialog(this, "Payment Successful! Thank you for your purchase.", "Order Confirmed", JOptionPane.INFORMATION_MESSAGE);
                    refreshCartTable(); // Cart is cleared after success
                } else {
                    JOptionPane.showMessageDialog(this, "Payment Failed. Please enter a valid 16-digit card number.", "Transaction Declined", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        return panel;
    }

    // --- UI Data Refresh Methods ---
    private void refreshCatalogTable() {
        catalogTableModel.setRowCount(0);
        for (Product p : store.getCatalog()) {
            catalogTableModel.addRow(new Object[]{
                p.getId(), p.getName(), p.getDescription(), String.format("%.2f", p.getPrice())
            });
        }
    }

    private void refreshCartTable() {
        cartTableModel.setRowCount(0);
        for (CartItem item : store.getCartItems()) {
            cartTableModel.addRow(new Object[]{
                item.getProduct().getName(),
                String.format("%.2f", item.getProduct().getPrice()),
                item.getQuantity(),
                String.format("%.2f", item.getSubtotal())
            });
        }
        totalLabel.setText("Total: $" + String.format("%.2f", store.calculateTotal()));
    }

    // =================================================================================
    // 4. MAIN METHOD
    // =================================================================================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new EcommerceGUI().setVisible(true);
        });
    }
}