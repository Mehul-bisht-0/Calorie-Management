import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JWindow;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;

public class Dashboard extends JFrame {
    private JPanel sideBar;
    private JPanel mainContent;
    private JTextField heightField, weightField, foodNameField, caloriesField, ageField;
    private JLabel totalCaloriesLabel, bmiResultLabel, calorieResultLabel, weightGoalLabel;
    private int dailyCalories = 0;
    private JComboBox<String> genderCombo, activityCombo;
    private JProgressBar calorieProgressBar;
    private JPanel foodLogPanel, bmiHistoryPanel;
    private final List<String> foodLog = new ArrayList<>();
    private JSlider weightGoalSlider;
    private final Color accentColor = new Color(83, 51, 237);
    private final Color secondaryColor = new Color(48, 35, 174);
    private final Map<String, Double> bmiHistory = new HashMap<>();
    private double calorieGoal = 2000; // Default goal
    private final Map<LocalDate, Integer> records = new HashMap<>();
    private Timer dailyResetTimer;

    public Dashboard() {
        setTitle("Fitness Tracker");
        setSize(1000, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        getContentPane().setBackground(new Color(245, 245, 250));

        createSideBar();
        add(sideBar, BorderLayout.WEST);

        createMainContent();
        add(mainContent, BorderLayout.CENTER);

        applyStyles();
        setLocationRelativeTo(null);

        loadRecordsFromCSV(); // Load existing records from the CSV file
        showLoadingScreen();
        startDailyResetTimer(); // Simulate a new day every 1 minute
    }

    private void createSideBar() {
        sideBar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, secondaryColor, 0, getHeight(), accentColor);
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                g2.dispose();
            }
        };
        sideBar.setLayout(new BoxLayout(sideBar, BoxLayout.Y_AXIS));
        sideBar.setPreferredSize(new Dimension(350, getHeight()));
        sideBar.setBorder(BorderFactory.createEmptyBorder(30, 25, 30, 25));

        JLabel titleLabel = new JLabel("Fitness Tracker");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        sideBar.add(titleLabel);
        sideBar.add(Box.createRigidArea(new Dimension(0, 50)));

        JButton[] buttons = {
            createStyledButton("Dashboard", "welcome"),
            createStyledButton("Track Calories", "calories"),
            createStyledButton("Calculate BMI", "bmi"),
            createStyledButton("Fitness Goals", "goals"),
            createStyledButton("Progress", "progress")
        };

        for (JButton button : buttons) {
            button.addActionListener(e -> handleButtonClick(e.getActionCommand()));
            sideBar.add(button);
            sideBar.add(Box.createRigidArea(new Dimension(0, 15)));
        }

        JButton recordsButton = createStyledButton("Monthly Records", "records");
        recordsButton.addActionListener(e -> showMonthlyRecords());
        sideBar.add(recordsButton);
        sideBar.add(Box.createRigidArea(new Dimension(0, 15)));

        sideBar.add(Box.createVerticalGlue());
        JPanel userProfile = new JPanel();
        userProfile.setLayout(new BoxLayout(userProfile, BoxLayout.Y_AXIS));
        userProfile.setOpaque(false);
        userProfile.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel userLabel = new JLabel("User Profile");
        userLabel.setForeground(Color.WHITE);
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        userLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton settingsButton = createStyledButton(" \u2699 Settings", "settings");
        settingsButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        userProfile.add(userLabel);
        userProfile.add(Box.createRigidArea(new Dimension(0, 10)));
        userProfile.add(settingsButton);

        sideBar.add(userProfile);
    }

    private void createMainContent() {
        mainContent = new JPanel(new CardLayout());
        mainContent.setBackground(Color.WHITE);

        createWelcomePanel();
        createCaloriePanel();
        createBMIPanel();
        createGoalsPanel();
        createProgressPanel();

        CardLayout cl = (CardLayout) mainContent.getLayout();
        cl.show(mainContent, "welcome");
    }

    private JPanel createFeatureCard(String title, String description) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(new Color(245, 245, 255));
        card.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 250), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        titleLabel.setForeground(secondaryColor);
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel descriptionLabel = new JLabel("<html><div style='text-align: center;'>" + description + "</div></html>");
        descriptionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        descriptionLabel.setForeground(new Color(100, 100, 100));
        descriptionLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        card.add(titleLabel);
        card.add(Box.createRigidArea(new Dimension(0, 10)));
        card.add(descriptionLabel);

        return card;
    }

    private void createWelcomePanel() {
        JPanel welcomePanel = new JPanel();
        welcomePanel.setLayout(new BoxLayout(welcomePanel, BoxLayout.Y_AXIS));
        welcomePanel.setBackground(Color.WHITE);
        welcomePanel.setBorder(BorderFactory.createEmptyBorder(50, 50, 50, 50));

        int hour = java.time.LocalTime.now().getHour();
        String greeting = (hour < 12) ? "Good Morning" : (hour < 18) ? "Good Afternoon" : "Good Evening";

        JLabel welcomeTitle = new JLabel(greeting + ", User!");
        welcomeTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        welcomeTitle.setForeground(secondaryColor);
        welcomeTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel welcomeSubtitle = new JLabel("Your personal fitness companion");
        welcomeSubtitle.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        welcomeSubtitle.setForeground(new Color(100, 100, 100));
        welcomeSubtitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomePanel.add(welcomeTitle);
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        welcomePanel.add(welcomeSubtitle);

        JPanel summaryPanel = new JPanel();
        summaryPanel.setBackground(new Color(245, 245, 255));
        summaryPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 250), 1, true),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        summaryPanel.setLayout(new BoxLayout(summaryPanel, BoxLayout.Y_AXIS));
        summaryPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        summaryPanel.setMaximumSize(new Dimension(600, 100));

        JLabel todayLabel = new JLabel("Today's Summary");
        todayLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        todayLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        welcomePanel.add(Box.createRigidArea(new Dimension(0, 20)));
        
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 20, 0));
        statsPanel.setOpaque(false);
        
        JLabel caloriesLabel = new JLabel("Calories: " + dailyCalories + " / " + (int)calorieGoal);
        caloriesLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel stepsLabel = new JLabel("Steps: 0 / 10,000");
        stepsLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        JLabel waterLabel = new JLabel("Water: 0 / 8 glasses");
        waterLabel.setHorizontalAlignment(SwingConstants.CENTER);
        
        statsPanel.add(caloriesLabel);
        statsPanel.add(stepsLabel);
        statsPanel.add(waterLabel);
        
        summaryPanel.add(todayLabel);
        summaryPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        summaryPanel.add(statsPanel);
        
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        welcomePanel.add(summaryPanel);

        // Add quick action buttons
        JPanel quickActionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 20));
        quickActionsPanel.setOpaque(false);
        quickActionsPanel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        String[][] quickActions = {
            {"\u2615", "Log Food", "calories"},
            {"\u2696", "Check BMI", "bmi"},
            {"\u26F9", "Set Goals", "goals"},
            {"\u1F4C8", "View Progress", "progress"}
        };
        
        for (String[] action : quickActions) {
            JButton actionButton = new JButton("<html><center>" + action[0] + "<br>" + action[1] + "</center></html>");
            actionButton.setPreferredSize(new Dimension(120, 80));
            actionButton.setBackground(new Color(245, 245, 255));
            actionButton.setForeground(secondaryColor);
            actionButton.setFocusPainted(false);
            actionButton.setBorder(new LineBorder(new Color(230, 230, 250), 1, true));
            actionButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
            actionButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            actionButton.setActionCommand(action[2]);
            
            actionButton.addActionListener(e -> handleButtonClick(e.getActionCommand()));
            
            // Add hover effect
            actionButton.addMouseListener(new java.awt.event.MouseAdapter() {
                @Override
                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    actionButton.setBackground(new Color(235, 235, 255));
                }
                @Override
                public void mouseExited(java.awt.event.MouseEvent evt) {
                    actionButton.setBackground(new Color(245, 245, 255));
                }
            });
            
            quickActionsPanel.add(actionButton);
        }
        
        welcomePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        welcomePanel.add(quickActionsPanel);

        // Add feature cards
        JPanel cardPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardPanel.setBackground(Color.WHITE);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        cardPanel.setAlignmentX(Component.CENTER_ALIGNMENT);

        String[][] features = {
            {"Track Calories", "Monitor your daily calorie intake"},
            {"Calculate BMI", "Check your Body Mass Index"},
            {"Set Goals", "Define and track your fitness goals"},
            {"Stay Healthy", "Get personalized recommendations"}
        };

        for (String[] feature : features) {
            cardPanel.add(createFeatureCard(feature[0], feature[1]));
        }

        welcomePanel.add(Box.createRigidArea(new Dimension(0, 30)));
        welcomePanel.add(cardPanel);

        mainContent.add(welcomePanel, "welcome");
    }

    private void createCaloriePanel() {
        JPanel caloriePanel = new JPanel();
        caloriePanel.setLayout(new BorderLayout(20, 20));
        caloriePanel.setBackground(new Color(240, 240, 240));
        caloriePanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Header section with calorie goal and progress
        JPanel headerPanel = new JPanel(new BorderLayout(15, 0));
        headerPanel.setOpaque(false);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel headerLabel = new JLabel("Calorie Tracker");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(secondaryColor);
        
        JPanel goalPanel = new JPanel(new BorderLayout(10, 0));
        goalPanel.setOpaque(false);
        
        totalCaloriesLabel = new JLabel("Today: " + dailyCalories + " / " + (int)calorieGoal + " kcal");
        totalCaloriesLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        calorieProgressBar = new JProgressBar(0, (int)calorieGoal);
        calorieProgressBar.setValue(dailyCalories);
        calorieProgressBar.setStringPainted(true);
        calorieProgressBar.setForeground(accentColor);
        calorieProgressBar.setString(dailyCalories + " / " + (int)calorieGoal + " kcal");
        
        JButton setGoalButton = new JButton("Set Goal");
        setGoalButton.setFocusPainted(false);
        setGoalButton.addActionListener(e -> {
            String input = JOptionPane.showInputDialog(this, "Enter your daily calorie goal:", calorieGoal);
            try {
                if (input != null && !input.isEmpty()) {
                    calorieGoal = Double.parseDouble(input);
                    calorieProgressBar.setMaximum((int)calorieGoal);
                    calorieProgressBar.setString(dailyCalories + " / " + (int)calorieGoal + " kcal");
                    totalCaloriesLabel.setText("Today: " + dailyCalories + " / " + (int)calorieGoal + " kcal");
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number");
            }
        });
        
        goalPanel.add(totalCaloriesLabel, BorderLayout.NORTH);
        goalPanel.add(calorieProgressBar, BorderLayout.CENTER);
        
        headerPanel.add(headerLabel, BorderLayout.WEST);
        headerPanel.add(goalPanel, BorderLayout.CENTER);
        headerPanel.add(setGoalButton, BorderLayout.EAST);
        
        // Food entry section
        JPanel entryPanel = new JPanel();
        entryPanel.setLayout(new BoxLayout(entryPanel, BoxLayout.Y_AXIS));
        entryPanel.setBackground(Color.WHITE);
        entryPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel entryLabel = new JLabel("Add Food Item");
        entryLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        entryLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JPanel inputPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        inputPanel.setOpaque(false);
        inputPanel.setMaximumSize(new Dimension(2000, 35));
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        foodNameField = new JTextField(20);
        foodNameField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        foodNameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        caloriesField = new JTextField(10);
        caloriesField.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        caloriesField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JPanel labelPanel = new JPanel(new GridLayout(1, 2, 15, 0));
        labelPanel.setOpaque(false);
        labelPanel.setMaximumSize(new Dimension(2000, 20));
        labelPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel foodNameLabel = new JLabel("Food Name");
        foodNameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        JLabel caloriesLabel = new JLabel("Calories (kcal)");
        caloriesLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        
        labelPanel.add(foodNameLabel);
        labelPanel.add(caloriesLabel);
        
        inputPanel.add(foodNameField);
        inputPanel.add(caloriesField);
        
        JButton addFoodBtn = new JButton("Add Food");
        addFoodBtn.setBackground(accentColor);
        addFoodBtn.setForeground(Color.WHITE);
        addFoodBtn.setFocusPainted(false);
        addFoodBtn.setBorderPainted(false);
        addFoodBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addFoodBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        addFoodBtn.setMaximumSize(new Dimension(150, 35));
        addFoodBtn.addActionListener(e -> addCalories());
        
        // Add predefined food options
        String[][] commonFoods = {
            {"Apple", "95"},
            {"Banana", "105"},
            {"Chicken Breast", "165"},
            {"Egg", "78"},
            {"Oatmeal", "150"},
            {"Salad", "50"}
        };
        
        JPanel quickFoodsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 10));
        quickFoodsPanel.setOpaque(false);
        quickFoodsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel quickLabel = new JLabel("Quick Add:");
        quickLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        quickFoodsPanel.add(quickLabel);
        
        for (String[] food : commonFoods) {
            JButton foodButton = new JButton(food[0]);
            foodButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            foodButton.setFocusPainted(false);
            foodButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
            foodButton.addActionListener(e -> {
                foodNameField.setText(food[0]);
                caloriesField.setText(food[1]);
            });
            quickFoodsPanel.add(foodButton);
        }
        
        entryPanel.add(entryLabel);
        entryPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        entryPanel.add(labelPanel);
        entryPanel.add(Box.createRigidArea(new Dimension(0, 5)));
        entryPanel.add(inputPanel);
        entryPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        entryPanel.add(addFoodBtn);
        entryPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        entryPanel.add(quickFoodsPanel);
        
        // Food log section
        JPanel logSection = new JPanel(new BorderLayout());
        logSection.setBackground(Color.WHITE);
        logSection.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel logLabel = new JLabel("Today's Food Log");
        logLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        
        foodLogPanel = new JPanel();
        foodLogPanel.setLayout(new BoxLayout(foodLogPanel, BoxLayout.Y_AXIS));
        foodLogPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(foodLogPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        JButton clearLogButton = new JButton("Clear Log");
        clearLogButton.setFocusPainted(false);
        clearLogButton.addActionListener(e -> {
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Are you sure you want to clear your food log?", 
                "Confirm", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                foodLogPanel.removeAll();
                foodLog.clear();
                dailyCalories = 0;
                totalCaloriesLabel.setText("Today: 0 / " + (int)calorieGoal + " kcal");
                calorieProgressBar.setValue(0);
                calorieProgressBar.setString("0 / " + (int)calorieGoal + " kcal");
                foodLogPanel.revalidate();
                foodLogPanel.repaint();
            }
        });
        
        JPanel logHeaderPanel = new JPanel(new BorderLayout());
        logHeaderPanel.setOpaque(false);
        logHeaderPanel.add(logLabel, BorderLayout.WEST);
        logHeaderPanel.add(clearLogButton, BorderLayout.EAST);
        
        logSection.add(logHeaderPanel, BorderLayout.NORTH);
        logSection.add(scrollPane, BorderLayout.CENTER);
        
        // Adding all components to the calorie panel
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setOpaque(false);
        
        centerPanel.add(entryPanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        centerPanel.add(logSection);
        
        caloriePanel.add(headerPanel, BorderLayout.NORTH);
        caloriePanel.add(centerPanel, BorderLayout.CENTER);
        
        mainContent.add(caloriePanel, "calories");
    }

    private JPanel createInputFieldWithLabel(String labelText, JTextField textField) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(labelText);
        label.setPreferredSize(new Dimension(100, 20));
        panel.add(label, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        panel.setMaximumSize(new Dimension(2000, 35));
        panel.setAlignmentX(Component.LEFT_ALIGNMENT);
        return panel;
    }

    private void createBMIPanel() {
        JPanel bmiPanel = new JPanel(new BorderLayout(20, 20));
        bmiPanel.setBackground(new Color(240, 240, 240));
        bmiPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Header
        JLabel headerLabel = new JLabel("BMI & Calorie Calculator");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(secondaryColor);
        
        // Input and result panels side by side
        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        contentPanel.setOpaque(false);
        
        // Input panel
        JPanel inputPanel = new JPanel();
        inputPanel.setLayout(new BoxLayout(inputPanel, BoxLayout.Y_AXIS));
        inputPanel.setBackground(Color.WHITE);
        inputPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel inputTitle = new JLabel("Enter Your Details");
        inputTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        inputTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // Create styled input fields
        JPanel heightInputPanel = createInputFieldWithLabel("Height (cm):", heightField = new JTextField(10));
        JPanel weightInputPanel = createInputFieldWithLabel("Weight (kg):", weightField = new JTextField(10));
        JPanel ageInputPanel = createInputFieldWithLabel("Age:", ageField = new JTextField(10));
        
        JPanel genderPanel = new JPanel(new BorderLayout());
        genderPanel.setOpaque(false);
        genderPanel.setMaximumSize(new Dimension(2000, 35));
        genderPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel genderLabel = new JLabel("Gender:");
        genderLabel.setPreferredSize(new Dimension(100, 20));
        genderCombo = new JComboBox<>(new String[]{"Male", "Female"});
        genderCombo.setBackground(Color.WHITE);
        
        genderPanel.add(genderLabel, BorderLayout.WEST);
        genderPanel.add(genderCombo, BorderLayout.CENTER);
        
        JPanel activityPanel = new JPanel(new BorderLayout());
        activityPanel.setOpaque(false);
        activityPanel.setMaximumSize(new Dimension(2000, 35));
        activityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        JLabel activityLabel = new JLabel("Activity Level:");
        activityLabel.setPreferredSize(new Dimension(100, 20));
        activityCombo = new JComboBox<>(new String[]{"Sedentary", "Light", "Moderate", "Active", "Very Active"});
        activityCombo.setBackground(Color.WHITE);
        
        activityPanel.add(activityLabel, BorderLayout.WEST);
        activityPanel.add(activityCombo, BorderLayout.CENTER);
        
        JButton calculateBtn = new JButton("Calculate");
        calculateBtn.setBackground(accentColor);
        calculateBtn.setForeground(Color.WHITE);
        calculateBtn.setFocusPainted(false);
        calculateBtn.setBorderPainted(false);
        calculateBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        calculateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        calculateBtn.setMaximumSize(new Dimension(150, 35));
        calculateBtn.addActionListener(e -> calculateBMIAndCalories());
        
        inputPanel.add(inputTitle);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        inputPanel.add(heightInputPanel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(weightInputPanel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(ageInputPanel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(genderPanel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        inputPanel.add(activityPanel);
        inputPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        inputPanel.add(calculateBtn);
        
        // Results panel
        JPanel resultsPanel = new JPanel();
        resultsPanel.setLayout(new BoxLayout(resultsPanel, BoxLayout.Y_AXIS));
        resultsPanel.setBackground(Color.WHITE);
        resultsPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel resultsTitle = new JLabel("Your Results");
        resultsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        resultsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        // BMI result display with visual indicator
        JPanel bmiResultPanel = new JPanel(new BorderLayout(10, 0));
        bmiResultPanel.setOpaque(false);
        bmiResultPanel.setMaximumSize(new Dimension(2000, 150));
        bmiResultPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        bmiResultLabel = new JLabel("BMI: Not calculated yet");
        bmiResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        
        // BMI scale visualization
        JPanel bmiScalePanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Draw scale
                int width = getWidth();
                int height = 25;
                int y = 10;
                
                // Colors for BMI ranges
                Color underweightColor = new Color(102, 204, 255);
                Color normalColor = new Color(102, 204, 102);
                Color overweightColor = new Color(255, 204, 102);
                Color obeseColor = new Color(255, 102, 102);
                
                // Draw scale segments
                g2d.setColor(underweightColor);
                g2d.fillRect(0, y, width/4, height);
                
                g2d.setColor(normalColor);
                g2d.fillRect(width/4, y, width/4, height);
                
                g2d.setColor(overweightColor);
                g2d.fillRect(width/2, y, width/4, height);
                
                g2d.setColor(obeseColor);
                g2d.fillRect(3*width/4, y, width/4, height);
                
                // Draw labels
                g2d.setColor(Color.BLACK);
                g2d.setFont(new Font("Arial", Font.PLAIN, 10));
                g2d.drawString("Underweight", 5, y + height + 15);
                g2d.drawString("Normal", width/4 + 5, y + height + 15);
                g2d.drawString("Overweight", width/2 + 5, y + height + 15);
                g2d.drawString("Obese", 3*width/4 + 5, y + height + 15);
            }
        };
        bmiScalePanel.setPreferredSize(new Dimension(200, 50));
        
        bmiResultPanel.add(bmiResultLabel, BorderLayout.NORTH);
        bmiResultPanel.add(bmiScalePanel, BorderLayout.CENTER);
        
        // Calorie result display
        calorieResultLabel = new JLabel("Daily Calorie Needs: Not calculated yet");
        calorieResultLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        calorieResultLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        resultsPanel.add(resultsTitle);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        resultsPanel.add(bmiResultPanel);
        resultsPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        resultsPanel.add(calorieResultLabel);
        
        contentPanel.add(inputPanel);
        contentPanel.add(resultsPanel);
        
        bmiPanel.add(headerLabel, BorderLayout.NORTH);
        bmiPanel.add(contentPanel, BorderLayout.CENTER);
        
        mainContent.add(bmiPanel, "bmi");
    }

    private void createGoalsPanel() {
        JPanel goalsPanel = new JPanel(new BorderLayout(20, 20));
        goalsPanel.setBackground(new Color(240, 240, 240));
        goalsPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Header
        JLabel headerLabel = new JLabel("Fitness Goals");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(secondaryColor);
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel goalsTitle = new JLabel("Set Your Weight Goal");
        goalsTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        goalsTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        weightGoalSlider = new JSlider(JSlider.HORIZONTAL, 0, 100, 50);
        weightGoalSlider.setMajorTickSpacing(10);
        weightGoalSlider.setMinorTickSpacing(1);
        weightGoalSlider.setPaintTicks(true);
        weightGoalSlider.setPaintLabels(true);
        weightGoalSlider.setBackground(Color.WHITE);
        weightGoalSlider.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        weightGoalLabel = new JLabel("Weight Goal: 50 kg");
        weightGoalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        weightGoalLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        weightGoalSlider.addChangeListener(e -> {
            int value = weightGoalSlider.getValue();
            weightGoalLabel.setText("Weight Goal: " + value + " kg");
        });
        
        JButton saveGoalBtn = new JButton("Save Goal");
        saveGoalBtn.setBackground(accentColor);
        saveGoalBtn.setForeground(Color.WHITE);
        saveGoalBtn.setFocusPainted(false);
        saveGoalBtn.setBorderPainted(false);
        saveGoalBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveGoalBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        saveGoalBtn.setMaximumSize(new Dimension(150, 35));
        saveGoalBtn.addActionListener(e -> {
            int goal = weightGoalSlider.getValue();
            JOptionPane.showMessageDialog(this, "Your weight goal of " + goal + " kg has been saved!");
        });
        
        contentPanel.add(goalsTitle);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(weightGoalSlider);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        contentPanel.add(weightGoalLabel);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        contentPanel.add(saveGoalBtn);
        
        goalsPanel.add(headerLabel, BorderLayout.NORTH);
        goalsPanel.add(contentPanel, BorderLayout.CENTER);
        
        mainContent.add(goalsPanel, "goals");
    }

    private void createProgressPanel() {
        JPanel progressPanel = new JPanel(new BorderLayout(20, 20));
        progressPanel.setBackground(new Color(240, 240, 240));
        progressPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        
        // Header
        JLabel headerLabel = new JLabel("Progress");
        headerLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        headerLabel.setForeground(secondaryColor);
        
        // Content panel
        JPanel contentPanel = new JPanel();
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(new Color(230, 230, 230), 1, true),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        
        JLabel progressTitle = new JLabel("BMI History");
        progressTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        progressTitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        bmiHistoryPanel = new JPanel();
        bmiHistoryPanel.setLayout(new BoxLayout(bmiHistoryPanel, BoxLayout.Y_AXIS));
        bmiHistoryPanel.setBackground(Color.WHITE);
        
        JScrollPane scrollPane = new JScrollPane(bmiHistoryPanel);
        scrollPane.setBorder(null);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        contentPanel.add(progressTitle);
        contentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        contentPanel.add(scrollPane);
        
        progressPanel.add(headerLabel, BorderLayout.NORTH);
        progressPanel.add(contentPanel, BorderLayout.CENTER);
        
        mainContent.add(progressPanel, "progress");
    }

    private JButton createStyledButton(String text, String actionCommand) {
        JButton button = new JButton(text);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorder(BorderFactory.createEmptyBorder(10, 50, 10, 50)); // Adjusted padding for better spacing
        button.setHorizontalAlignment(SwingConstants.LEFT); // Align text to the left
        button.setForeground(Color.WHITE);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setActionCommand(actionCommand);
        
        button.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                button.setForeground(new Color(200, 200, 255));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                button.setForeground(Color.WHITE);
            }
        });
        
        return button;
    }

    private void handleButtonClick(String actionCommand) {
        CardLayout cl = (CardLayout) mainContent.getLayout();
        cl.show(mainContent, actionCommand);
    }

    private void addCalories() {
        String foodName = foodNameField.getText();
        String caloriesText = caloriesField.getText();
        
        if (foodName.isEmpty() || caloriesText.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter both food name and calories.");
            return;
        }
        
        try {
            int calories = Integer.parseInt(caloriesText);
            dailyCalories += calories;
            totalCaloriesLabel.setText("Today: " + dailyCalories + " / " + (int)calorieGoal + " kcal");
            calorieProgressBar.setValue(dailyCalories);
            calorieProgressBar.setString(dailyCalories + " / " + (int)calorieGoal + " kcal");
            
            JLabel foodLogEntry = new JLabel(foodName + " - " + calories + " kcal");
            foodLogEntry.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            foodLogPanel.add(foodLogEntry);
            foodLogPanel.revalidate();
            foodLogPanel.repaint();
            
            foodLog.add(foodName + " - " + calories + " kcal");
            
            foodNameField.setText("");
            caloriesField.setText("");
            
            // Save daily calories to records
            LocalDate today = LocalDate.now();
            records.put(today, dailyCalories);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid number for calories.");
        }
    }

    private void calculateBMIAndCalories() {
        try {
            double height = Double.parseDouble(heightField.getText()) / 100; // Convert cm to meters
            double weight = Double.parseDouble(weightField.getText());
            int age = Integer.parseInt(ageField.getText());
            String gender = (String) genderCombo.getSelectedItem();
            String activityLevel = (String) activityCombo.getSelectedItem();
            
            // Calculate BMI
            double bmi = weight / (height * height);
            DecimalFormat df = new DecimalFormat("#.##");
            bmiResultLabel.setText("BMI: " + df.format(bmi));
            
            // Calculate daily calorie needs using Mifflin-St Jeor Equation
            double bmr;
            if (gender.equals("Male")) {
                bmr = 10 * weight + 6.25 * (height * 100) - 5 * age + 5;
            } else {
                bmr = 10 * weight + 6.25 * (height * 100) - 5 * age - 161;
            }
            
            double activityMultiplier;
            activityMultiplier = switch (activityLevel) {
                case "Sedentary" -> 1.2;
                case "Light" -> 1.375;
                case "Moderate" -> 1.55;
                case "Active" -> 1.725;
                case "Very Active" -> 1.9;
                default -> 1.2;
            };
            
            double calculatedDailyCalories = bmr * activityMultiplier;
            calorieResultLabel.setText("Daily Calorie Needs: " + df.format(calculatedDailyCalories) + " kcal");
            
            // Save BMI history
            String date = java.time.LocalDate.now().toString();
            bmiHistory.put(date, bmi);
            updateBMIHistoryPanel();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter valid numbers for height, weight, and age.");
        }
    }

    private void updateBMIHistoryPanel() {
        bmiHistoryPanel.removeAll();
        
        for (Map.Entry<String, Double> entry : bmiHistory.entrySet()) {
            JLabel historyEntry = new JLabel(entry.getKey() + ": " + entry.getValue());
            historyEntry.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            bmiHistoryPanel.add(historyEntry);
        }
        
        bmiHistoryPanel.revalidate();
        bmiHistoryPanel.repaint();
    }

    private void loadRecordsFromCSV() {
        String filePath = "monthly_calorie_records.csv";
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            boolean skipHeader = true; // Skip the header row
            while ((line = reader.readLine()) != null) {
                if (skipHeader) {
                    skipHeader = false;
                    continue;
                }
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    LocalDate date = LocalDate.parse(parts[0]);
                    int calories = Integer.parseInt(parts[1]);
                    records.put(date, calories);
                }
            }
        } catch (IOException e) {
            System.out.println("No existing records found or error reading file: " + e.getMessage());
        }
    }

    private void exportRecordsToCSV() {
        String filePath = "monthly_calorie_records.csv";
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.append("Day,Total Calories\n"); // CSV header

            // Get all dates and sort them chronologically
            List<LocalDate> sortedDates = new ArrayList<>(records.keySet());
            sortedDates.sort(LocalDate::compareTo);

            // Write records to CSV
            for (LocalDate date : sortedDates) {
                writer.append(date.toString())
                      .append(",")
                      .append(String.valueOf(records.get(date)))
                      .append("\n");
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Error exporting records: " + e.getMessage(), 
                "Export Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void startDailyResetTimer() {
        dailyResetTimer = new Timer(true); // Daemon thread
        dailyResetTimer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                resetDailyData();
            }
        }, 0, 60 * 1000); // Simulate a new day every 1 minute
    }

    private void resetDailyData() {
        LocalDate today = LocalDate.now();

        // Save today's data if it exists and hasn't been saved yet
        if (!records.containsKey(today)) {
            records.put(today, dailyCalories);
        }

        // Add next day's entry
        LocalDate nextDay = today.plusDays(1);
        if (!records.containsKey(nextDay)) {
            records.put(nextDay, 0); // Initialize the next day's record with 0 calories
        }

        // Export to CSV to sync the records
        exportRecordsToCSV();

        // Reset daily tracking
        dailyCalories = 0;
        totalCaloriesLabel.setText("Today: 0 / " + (int) calorieGoal + " kcal");
        calorieProgressBar.setValue(0);
        calorieProgressBar.setString("0 / " + (int) calorieGoal + " kcal");
        foodLogPanel.removeAll();
        foodLog.clear();

        // Update UI
        foodLogPanel.revalidate();
        foodLogPanel.repaint();

        // Maintain only the last 31 days
        if (records.size() > 31) {
            LocalDate oldestDate = records.keySet().stream()
                .min(LocalDate::compareTo)
                .orElse(null);
            if (oldestDate != null) {
                records.remove(oldestDate);
            }
        }
    }

    private void showMonthlyRecords() {
        JFrame recordsFrame = new JFrame("Monthly Records");
        recordsFrame.setSize(900, 600);
        recordsFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        recordsFrame.setLayout(new BorderLayout());
        recordsFrame.getContentPane().setBackground(new Color(245, 245, 250));

        JLabel titleLabel = new JLabel("Calorie Records for the Month", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(48, 35, 174));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));
        recordsFrame.add(titleLabel, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 20, 0)); // Split into two columns
        contentPanel.setBackground(new Color(245, 245, 250));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        // Left panel for text records
        JTextArea recordsArea = new JTextArea();
        recordsArea.setEditable(false);
        recordsArea.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        recordsArea.setForeground(new Color(60, 60, 60));
        recordsArea.setBackground(new Color(255, 255, 255));
        recordsArea.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        StringBuilder recordSummary = new StringBuilder();
        recordSummary.append("Day, Total Calories\n");

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Add data directly from records map instead of reading CSV
        List<LocalDate> sortedDates = new ArrayList<>(records.keySet());
        sortedDates.sort(LocalDate::compareTo);
        
        for (LocalDate date : sortedDates) {
            int calories = records.get(date);
            dataset.addValue(calories, "Calories Eaten", date.toString());
            dataset.addValue((int) calorieGoal, "Calorie Goal", date.toString());
            recordSummary.append(date.toString()).append(", ").append(calories).append("\n");
        }

        recordsArea.setText(recordSummary.toString());
        JScrollPane scrollPane = new JScrollPane(recordsArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
            "Records",
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Segoe UI", Font.BOLD, 14),
            new Color(48, 35, 174)
        ));

        // Right panel for the graph
        JFreeChart lineChart = ChartFactory.createLineChart(
            "Calorie Progress",
            "Day",
            "Calories",
            dataset
        );
        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new Dimension(400, 400));

        // Add components to the content panel
        contentPanel.add(scrollPane); // Left: Text records
        contentPanel.add(chartPanel); // Right: Graph

        recordsFrame.add(contentPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setBackground(new Color(245, 245, 250));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JButton exportButton = new JButton("Export to CSV");
        exportButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        exportButton.setBackground(new Color(83, 51, 237));
        exportButton.setForeground(Color.WHITE);
        exportButton.setFocusPainted(false);
        exportButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        exportButton.addActionListener(e -> exportRecordsToCSV());
        buttonPanel.add(exportButton);

        JButton closeButton = new JButton("Close");
        closeButton.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        closeButton.setBackground(new Color(200, 200, 200));
        closeButton.setForeground(Color.BLACK);
        closeButton.setFocusPainted(false);
        closeButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        closeButton.addActionListener(e -> recordsFrame.dispose());
        buttonPanel.add(closeButton);

        recordsFrame.add(buttonPanel, BorderLayout.SOUTH);

        recordsFrame.setLocationRelativeTo(this);
        recordsFrame.setVisible(true);
    }

    private void applyStyles() {
        // Apply modern styling to components
        UIManager.put("Button.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Label.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TextField.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("ComboBox.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("TextArea.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("List.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Table.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("Menu.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("MenuItem.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("CheckBox.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("RadioButton.font", new Font("Segoe UI", Font.PLAIN, 14));
        UIManager.put("ToolTip.font", new Font("Segoe UI", Font.PLAIN, 14));
    }

    private void showLoadingScreen() {
        JWindow loadingScreen = new JWindow();
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(new Color(83, 51, 237));
        JLabel loadingLabel = new JLabel("Loading...", SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        loadingLabel.setForeground(Color.WHITE);
        content.add(loadingLabel, BorderLayout.CENTER);
        loadingScreen.getContentPane().add(content);
        loadingScreen.setSize(300, 200);
        loadingScreen.setLocationRelativeTo(null);
        loadingScreen.setVisible(true);
        
        // Simulate loading time
        new javax.swing.Timer(2000, e -> loadingScreen.dispose()).start();
    }

}