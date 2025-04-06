import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.util.*;

public class QuizSystem {
    private static final String USERS_FILE = "users.json";
    private static final String QUIZ_FILE = "quiz.json";
    private static JSONArray users;
    private static JSONArray quizQuestions;
    private static Scanner scanner = new Scanner(System.in);
    private static JSONParser parser = new JSONParser();

    public static void main(String[] args) {
        initializeFiles();
        login();
    }

    private static void initializeFiles() {
        try {
            // Initialize users.json if not exists
            File usersFile = new File(USERS_FILE);
            if (!usersFile.exists()) {
                usersFile.createNewFile();
                JSONArray defaultUsers = new JSONArray();

                JSONObject admin = new JSONObject();
                admin.put("username", "admin");
                admin.put("password", "1234");
                admin.put("role", "admin");

                JSONObject student = new JSONObject();
                student.put("username", "salman");
                student.put("password", "1234");
                student.put("role", "student");

                defaultUsers.add(admin);
                defaultUsers.add(student);

                FileWriter writer = new FileWriter(USERS_FILE);
                writer.write(defaultUsers.toJSONString());
                writer.flush();
                writer.close();
            }

            // Initialize quiz.json if not exists
            File quizFile = new File(QUIZ_FILE);
            if (!quizFile.exists()) {
                quizFile.createNewFile();
                FileWriter writer = new FileWriter(QUIZ_FILE);
                writer.write(new JSONArray().toJSONString());
                writer.flush();
                writer.close();
            }

            // Load existing data
            users = (JSONArray) parser.parse(new FileReader(USERS_FILE));
            quizQuestions = (JSONArray) parser.parse(new FileReader(QUIZ_FILE));

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    private static void login() {
        System.out.print("System:> Enter your username\nUser:> ");
        String username = scanner.nextLine();

        System.out.print("System:> Enter password\nUser:> ");
        String password = scanner.nextLine();

        JSONObject user = authenticate(username, password);

        if (user != null) {
            String role = (String) user.get("role");
            if ("admin".equals(role)) {
                adminMenu();
            } else {
                studentMenu();
            }
        } else {
            System.out.println("System:> Invalid credentials. Try again.");
            login();
        }
    }

    private static JSONObject authenticate(String username, String password) {
        for (Object obj : users) {
            JSONObject user = (JSONObject) obj;
            if (username.equals(user.get("username")) && password.equals(user.get("password"))) {
                return user;
            }
        }
        return null;
    }

    private static void adminMenu() {
        System.out.println("System:> Welcome admin! Please create new questions in the question bank.");

        char choice;
        do {
            System.out.print("System:> Input your question\nAdmin:> ");
            String question = scanner.nextLine();

            JSONObject mcq = new JSONObject();
            mcq.put("question", question);

            for (int i = 1; i <= 4; i++) {
                System.out.print("System: Input option " + i + ":\nAdmin:> ");
                String option = scanner.nextLine();
                mcq.put("option " + i, option);
            }

            System.out.print("System: What is the answer key? (1-4)\nAdmin:> ");
            int answerKey = Integer.parseInt(scanner.nextLine());
            mcq.put("answerkey", answerKey);

            quizQuestions.add(mcq);
            saveQuizQuestions();

            System.out.print("System:> Saved successfully! Do you want to add more questions? (press any key to continue or q to quit)\nAdmin:> ");
            choice = scanner.nextLine().toLowerCase().charAt(0);
        } while (choice != 'q');
    }

    private static void studentMenu() {
        System.out.print("System:> Welcome to the quiz! We will throw you 10 questions. Each MCQ mark is 1 and no negative marking. Are you ready? Press 's' to start.\nStudent:> ");
        char choice = scanner.nextLine().toLowerCase().charAt(0);
        if (choice != 's') {
            return;
        }

        if (quizQuestions.size() < 10) {
            System.out.println("System:> Not enough questions in the quiz bank. Please ask admin to add more questions.");
            return;
        }

        int score = 0;
        Random random = new Random();
        List<Integer> shownQuestions = new ArrayList<>();

        for (int i = 1; i <= 10; i++) {
            int questionIndex;
            do {
                questionIndex = random.nextInt(quizQuestions.size());
            } while (shownQuestions.contains(questionIndex));

            shownQuestions.add(questionIndex);

            JSONObject question = (JSONObject) quizQuestions.get(questionIndex);
            System.out.println("\n[Question " + i + "] " + question.get("question"));
            for (int j = 1; j <= 4; j++) {
                System.out.println(j + ". " + question.get("option " + j));
            }

            System.out.print("\nStudent:> ");
            try {
                int answer = Integer.parseInt(scanner.nextLine());
                if (answer == ((Long) question.get("answerkey")).intValue()) {
                    score++;
                }
            } catch (NumberFormatException e) {
                // Invalid input, score remains same
            }
        }

        displayResult(score);

        System.out.print("\nWould you like to start again? press s for start or q for quit\nStudent:> ");
        choice = scanner.nextLine().toLowerCase().charAt(0);
        if (choice == 's') {
            studentMenu();
        }
    }

    private static void displayResult(int score) {
        System.out.println("\nSystem:> Your result:");

        if (score >= 8) {
            System.out.println("Excellent! You have got " + score + " out of 10");
        } else if (score >= 5) {
            System.out.println("Good. You have got " + score + " out of 10");
        } else if (score >= 2) {
            System.out.println("Very poor! You have got " + score + " out of 10");
        } else {
            System.out.println("Very sorry you are failed. You have got " + score + " out of 10");
        }
    }

    private static void saveQuizQuestions() {
        try (FileWriter writer = new FileWriter(QUIZ_FILE)) {
            writer.write(quizQuestions.toJSONString());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}