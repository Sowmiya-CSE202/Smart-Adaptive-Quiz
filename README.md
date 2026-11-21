# Smart Adaptive Quiz

A full-stack, rule-based adaptive learning platform built with **Spring Boot 3** and modern vanilla frontend (HTML/CSS/JS). It extracts key concepts from uploaded study PDF materials using Apache PDFBox, dynamically generates adaptive quiz questions, builds an interactive word-search puzzle, and provides analytics with a mistake analyzer.

---

## 🚀 Features

- **📄 PDF Study Material Processing**: Upload any text-based study document in PDF format to automatically extract keywords and technical concepts.
- **🎯 Dynamic Question Generation**: Automatically creates context-aware multiple-choice questions with dynamic distractors.
- **📈 Adaptive Difficulty Engine**: Tracks streak counts; 3 consecutive correct answers elevate difficulty, while 2 mistakes lower difficulty.
- **🧩 Word-Search Puzzle Generator**: Procedurally builds a multi-directional letter grid containing the extracted keywords.
- **📊 Real-time Learning Dashboard**: Displays topic accuracy breakdown, response time metrics, and answer distribution.
- **🔍 Mistake Analyzer**: Groups incorrect responses by topic to highlight areas that need revision.

---

## 🛠️ Tech Stack

- **Backend**: Java 17, Spring Boot 3.4.2, Spring Data JPA, Apache PDFBox 3.0.3, H2 Database (File-based)
- **Frontend**: HTML5, CSS3, Vanilla JavaScript (ES6+)
- **Build Tool**: Maven

---

## 📦 Project Structure

```
SmartAdaptiveQuiz/
├── pom.xml
└── src/
    └── main/
        ├── java/
        │   └── com/smartquiz/
        │       ├── SmartAdaptiveQuizApplication.java
        │       ├── controller/
        │       │   └── ApiController.java
        │       ├── model/
        │       │   ├── Difficulty.java
        │       │   ├── QuizQuestion.java
        │       │   ├── QuizSession.java
        │       │   └── QuestionAttempt.java
        │       ├── repository/
        │       │   ├── QuizSessionRepository.java
        │       │   └── QuestionAttemptRepository.java
        │       └── service/
        │           ├── ContentStore.java
        │           ├── PdfService.java
        │           ├── QuestionGenerator.java
        │           └── WordSearchGenerator.java
        └── resources/
            ├── application.properties
            └── static/
                ├── index.html
                ├── style.css
                └── app.js
```

---

## 🏃 Running the Application

### Prerequisites
- Java 17 or higher
- Maven 3.8+

### Build and Start
```bash
# Clean and install dependencies
mvn clean install

# Run the Spring Boot application
mvn spring-boot:run
```

Once started, open your web browser and navigate to:
- **Application UI**: [http://localhost:8080](http://localhost:8080)
- **H2 Database Console**: [http://localhost:8080/h2-console](http://localhost:8080/h2-console)
  - **JDBC URL**: `jdbc:h2:file:./data/smartquiz`
  - **User Name**: `sa`
  - **Password**: *(leave blank)*
