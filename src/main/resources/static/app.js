const $ = id => document.getElementById(id);

let questions = [];

let questionIndex = 0;

let selectedAnswer = "";

let questionStarted = 0;

let foundWords = new Set();

let selectedLetters = [];

// SIDEBAR NAVIGATION

document.querySelectorAll(".nav").forEach(button => {

    button.addEventListener("click", () => {

        showView(button.dataset.view);

    });

});

function showView(view) {

    document.querySelectorAll(".nav").forEach(button => {

        button.classList.toggle(
            "active",
            button.dataset.view === view
        );

    });

    document.querySelectorAll(".view").forEach(section => {

        section.classList.toggle(
            "active",
            section.id === view
        );

    });

    const titles = {

        dashboard: "Dashboard",
        upload: "Upload PDF",
        puzzle: "Word Search Puzzle",
        quiz: "Adaptive Quiz",
        mistakes: "Mistake Analyzer",
        progress: "Progress"

    };

    $("pageTitle").textContent = titles[view];

    if (view === "dashboard") {
        loadDashboard();
    }

    if (view === "mistakes") {
        loadMistakes();
    }

    if (view === "progress") {
        loadDashboard();
    }
}

// API REQUEST HANDLER

async function api(url, options = {}) {

    const response = await fetch(url, options);

    const data = await response.json();

    if (!response.ok) {

        throw new Error(
            data.error || "Request failed"
        );

    }

    return data;
}

// PDF UPLOAD

$("uploadBtn").onclick = async () => {

    const file = $("pdfFile").files[0];

    if (!file) {

        $("uploadMessage").textContent =
            "Select a PDF file first.";

        return;
    }

    const formData = new FormData();

    formData.append("file", file);

    $("uploadMessage").textContent =
        "Processing PDF...";

    try {

        const data = await api(
            "/api/pdf/upload",
            {
                method: "POST",
                body: formData
            }
        );

        $("uploadMessage").textContent =
            `${data.message}. ${data.questionsGenerated} questions generated.`;

        await loadQuestions();

        await loadPuzzle();

        await loadDashboard();

    } catch (error) {

        $("uploadMessage").textContent =
            error.message;

    }
};

// LOAD QUIZ QUESTIONS

async function loadQuestions() {

    const data = await api(
        "/api/quiz/questions"
    );

    questions = data;

    questionIndex = 0;

    renderQuestion();
}

// DISPLAY QUESTION

function renderQuestion() {

    if (!questions.length) {
        return;
    }

    const question = questions[questionIndex];

    $("qCount").textContent =
        `Question ${questionIndex + 1} of ${questions.length}`;

    $("qDifficulty").textContent =
        `${question.topic} · ${question.difficulty}`;

    $("questionText").textContent =
        question.prompt;

    selectedAnswer = "";

    $("quizFeedback").textContent = "";

    $("nextQuestion").disabled = true;

    $("nextQuestion").textContent =
        "Submit answer";

    $("options").innerHTML = "";

    question.options.forEach(option => {

        const button =
            document.createElement("button");

        button.className = "option";

        button.textContent = option;

        button.onclick = () => {

            selectedAnswer = option;

            document.querySelectorAll(".option")
                .forEach(item => {
                    item.classList.remove("selected");
                });

            button.classList.add("selected");

            $("nextQuestion").disabled = false;

        };

        $("options").appendChild(button);

    });

    questionStarted = Date.now();
}

// QUIZ ANSWER SUBMISSION

$("nextQuestion").onclick = submitHandler;

async function submitHandler() {

    if (!selectedAnswer) {
        return;
    }

    const question = questions[questionIndex];

    try {

        const data = await api(
            "/api/quiz/submit",
            {
                method: "POST",

                headers: {
                    "Content-Type": "application/json"
                },

                body: JSON.stringify({

                    questionId: question.id,

                    answer: selectedAnswer,

                    responseMillis:
                        Date.now() - questionStarted

                })
            }
        );

        $("quizFeedback").textContent =
            data.message +
            ` Correct answer: ${data.correctAnswer}. Difficulty: ${data.difficulty}`;

        // Disable option buttons so user cannot re-select after submission
        document.querySelectorAll(".option").forEach(btn => btn.disabled = true);

        $("nextQuestion").disabled = false;

        $("nextQuestion").textContent =
            "Next question";

        $("nextQuestion").onclick = () => {

            questionIndex =
                (questionIndex + 1) % questions.length;

            renderQuestion();

            $("nextQuestion").onclick = submitHandler;

        };

    } catch (error) {

        $("quizFeedback").textContent =
            error.message;

    }
}

// WORD SEARCH PUZZLE

async function loadPuzzle() {

    const data = await api("/api/puzzle");

    const grid = $("grid");

    const size = data.grid.length;

    grid.style.setProperty("--size", size);

    grid.innerHTML = "";

    selectedLetters = [];

    foundWords.clear();

    data.grid.forEach((row, r) => {

        row.forEach((character, c) => {

            const button =
                document.createElement("button");

            button.className = "letter";

            button.textContent = character;

            button.dataset.r = r;

            button.dataset.c = c;

            button.onclick = () => {

                button.classList.toggle("selected");

                const key = `${r},${c}`;

                if (selectedLetters.includes(key)) {

                    selectedLetters =
                        selectedLetters.filter(
                            item => item !== key
                        );

                } else {

                    selectedLetters.push(key);

                }

                $("wordSelection").value =
                    selectedLetters.map(key => {

                        const [row, col] =
                            key.split(",").map(Number);

                        return data.grid[row][col];

                    }).join("");

            };

            grid.appendChild(button);

        });

    });

    $("wordList").innerHTML = "";

    data.words.forEach(word => {

        const li = document.createElement("li");

        li.textContent = word;

        li.id = "word-" + word;

        $("wordList").appendChild(li);

    });

    $("checkWord").onclick = () => {

        const word = $("wordSelection")
            .value
            .toUpperCase()
            .replace(/[^A-Z]/g, "");

        if (data.words.includes(word)) {

            foundWords.add(word);

            $("word-" + word).classList.add("found");

            $("puzzleMessage").textContent =
                `${word} found! ${foundWords.size}/${data.words.length}`;

            document.querySelectorAll(".letter.selected")
                .forEach(button => {

                    button.classList.remove("selected");

                    button.classList.add("found");

                });

        } else {

            $("puzzleMessage").textContent =
                "That word is not in the target list.";

            document.querySelectorAll(".letter.selected")
                .forEach(button => {
                    button.classList.remove("selected");
                });

        }

        selectedLetters = [];

        $("wordSelection").value = "";

    };

}

// DASHBOARD

async function loadDashboard() {

    try {

        const data = await api("/api/dashboard");

        $("stats").innerHTML = [

            [
                "Questions Attempted",
                data.questionsAttempted
            ],

            [
                "Overall Accuracy",
                data.accuracy + "%"
            ],

            [
                "Average Time",
                (data.averageTimeMillis / 1000).toFixed(1) + "s"
            ],

            [
                "Topics Practiced",
                data.topicsPracticed
            ]

        ].map(item => `

            <div class="stat">

                <small>${item[0]}</small>

                <strong>${item[1]}</strong>

            </div>

        `).join("");

        $("topicBars").innerHTML =

            Object.entries(data.topicAccuracy)
                .map(([topic, accuracy]) => `

                    <div class="bar-row">

                        <span>${escapeHtml(topic)}</span>

                        <div class="track">

                            <div class="fill"
                                 style="width:${accuracy}%">
                            </div>

                        </div>

                        <b>${accuracy.toFixed(0)}%</b>

                    </div>

                `).join("") || "No attempts yet.";

        const distribution =
            data.answerDistribution;

        $("distribution").textContent =
            `Correct: ${distribution.correct} · Incorrect: ${distribution.incorrect}`;

        $("weakTopics").textContent =
            data.weakTopics.join(", ") ||
            "No weak topics identified yet.";

        renderActivity(data.recentActivity);

        $("progressContent").textContent =

            `${data.questionsAttempted} questions attempted with ${data.accuracy}% accuracy across ${data.topicsPracticed} topics.`;

    } catch (error) {

        $("stats").textContent =
            "Upload a PDF to begin.";

    }
}

// RECENT ACTIVITY TABLE

function renderActivity(rows) {

    $("activity").innerHTML =

        rows.map(item => `

            <tr>

                <td>${escapeHtml(item.topic)}</td>

                <td>${escapeHtml(item.answer)}</td>

                <td class="${item.correct ? "good" : "bad"}">

                    ${item.correct ? "Correct" : "Incorrect"}

                </td>

                <td>
                    ${(item.responseMillis / 1000).toFixed(1)}s
                </td>

                <td>
                    ${new Date(item.attemptedAt).toLocaleString()}
                </td>

            </tr>

        `).join("") ||

        '<tr><td colspan="5">No recent activity.</td></tr>';
}

// ACTIVITY FILTER

$("activityFilter").oninput = async () => {

    try {

        const data = await api("/api/dashboard");

        const filtered =
            data.recentActivity.filter(item =>

                item.topic.toLowerCase().includes(
                    $("activityFilter").value.toLowerCase()
                )

            );

        renderActivity(filtered);

    } catch (error) {

        // Dashboard data unavailable.

    }

};

// MISTAKE ANALYZER

async function loadMistakes() {

    try {

        const data = await api("/api/mistakes");

        $("mistakeContent").innerHTML = `

            <p>
                Total incorrect answers:
                <b>${data.totalMistakes}</b>
            </p>

        ` +

        Object.entries(data.mistakesByTopic)
            .map(([topic, count]) => `

                <p>
                    ${escapeHtml(topic)}:
                    ${count} mistake(s)
                </p>

            `).join("");

    } catch (error) {

        $("mistakeContent").textContent =
            error.message;

    }
}

// HTML ESCAPING

function escapeHtml(value) {

    return String(value).replace(
        /[&<>"']/g,

        character => ({

            "&": "&amp;",
            "<": "&lt;",
            ">": "&gt;",
            '"': "&quot;",
            "'": "&#39;"

        })[character]

    );
}

// INITIAL DASHBOARD LOAD

loadDashboard();
