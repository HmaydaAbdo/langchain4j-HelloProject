# 🤖 AI Assistant Demo — LangChain4j + Ollama + Spring Boot

A Spring Boot backend showcasing **4 AI-powered services** built with [LangChain4j](https://github.com/langchain4j/langchain4j) and [Ollama](https://ollama.com), running a local `mistral` LLM — no external API keys required.

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Spring Boot App                       │
│                                                         │
│  ┌──────────────┐   ┌─────────────────────────────────┐ │
│  │  Controllers │   │           Services              │ │
│  │              │   │                                 │ │
│  │  /recipe     │──▶│  Assistant (@AiService)         │ │
│  │  /story      │──▶│  StreamingAssistant (@AiService)│ │
│  │  /travel     │──▶│  ChatService (ChatLangModel)    │ │
│  │  /code-review│──▶│  StreamingChatService (Stream)  │ │
│  └──────────────┘   └──────────────┬────────────────┘  │
└─────────────────────────────────────┼───────────────────┘
                                      │ HTTP
                              ┌───────▼───────┐
                              │    Ollama      │
                              │  (mistral LLM) │
                              │ localhost:11434 │
                              └───────────────┘
```

---

## 🚀 Services Overview

| Endpoint | Service | LangChain4j API | Use Case |
|---|---|---|---|
| `GET /recipe` | `Assistant` | `@AiService` + `@SystemMessage` + `@V` | 🍳 Recipe advisor |
| `GET /story/stream` | `StreamingAssistant` | `@AiService` + `TokenStream` | 📖 Story generator (SSE) |
| `GET /travel` | `ChatService` | `ChatLanguageModel` | ✈️ Travel itinerary |
| `POST /code-review` | `StreamingChatService` | `StreamingChatLanguageModel` | 🔍 Code review (SSE) |

---

## 🛠️ Tech Stack

- **Java 17** / **Spring Boot 4.x**
- **LangChain4j 0.36.2** — AI service framework
- **Ollama** — local LLM runtime
- **Mistral** — open-source LLM (runs fully offline)
- **Docker & Docker Compose**

---

## ⚡ Quick Start with Docker

### Prerequisites
- [Docker](https://www.docker.com/get-started) & Docker Compose installed
- At least **6 GB of free disk space** (for the mistral model)

### 1. Clone the repository

```bash
git clone https://github.com/hmayda/langchain4j-HelloProject.git
cd langchain4j-HelloProject
```

### 2. Start everything

```bash
docker compose up --build
```

This will:
1. Start the **Ollama** container
2. Automatically pull the **mistral** model (~4 GB, first run only)
3. Build and start the **Spring Boot** app

### 3. Wait for startup

Once you see this log line the app is ready:

```
Started AiAssistantDemoApplication in X seconds
```

### 4. Try an endpoint

```bash
curl "http://localhost:8080/recipe?ingredients=pasta,tomatoes,garlic&diet=vegetarian"
```

---

## 💻 Local Development (without Docker)

### Prerequisites
- Java 17+
- Maven 3.9+
- [Ollama installed](https://ollama.com/download)

### 1. Pull the model

```bash
ollama pull mistral
```

### 2. Start Ollama

```bash
ollama serve
```

### 3. Run the app

```bash
cd ai-assistant-backend
./mvnw spring-boot:run
```

---

## 📡 API Reference

### 🍳 Recipe Advisor
> Uses `@AiService` with `@SystemMessage`, `@UserMessage` and `@V` template variables

```
GET /recipe?ingredients={ingredients}&diet={diet}
```

| Parameter | Required | Default | Example |
|---|---|---|---|
| `ingredients` | ✅ | — | `chicken,lemon,garlic` |
| `diet` | ❌ | `any` | `vegetarian`, `vegan`, `gluten-free` |

**Example:**
```bash
curl "http://localhost:8080/recipe?ingredients=eggs,spinach,feta&diet=vegetarian"
```

---

### 📖 Story Generator (Streaming SSE)
> Uses `@AiService` returning `TokenStream` — tokens streamed in real-time

```
GET /story/stream?genre={genre}&character={character}&setting={setting}
```

| Parameter | Required | Default | Example |
|---|---|---|---|
| `genre` | ❌ | `adventure` | `thriller`, `fantasy`, `sci-fi` |
| `character` | ✅ | — | `Yassine`, `Aria` |
| `setting` | ❌ | `a futuristic city` | `Marrakech medina at night` |

**Example:**
```bash
curl -N "http://localhost:8080/story/stream?genre=thriller&character=Yassine&setting=Marrakech+medina+at+night"
```

---

### ✈️ Travel Itinerary
> Uses `ChatLanguageModel` directly with a hand-crafted prompt

```
GET /travel?destination={destination}&days={days}&style={style}
```

| Parameter | Required | Default | Example |
|---|---|---|---|
| `destination` | ✅ | — | `Tokyo`, `Marrakech` |
| `days` | ❌ | `3` | `5`, `7` |
| `style` | ❌ | `cultural` | `adventure`, `relaxed`, `foodie` |

**Example:**
```bash
curl "http://localhost:8080/travel?destination=Marrakech&days=3&style=foodie"
```

---

### 🔍 Code Review (Streaming SSE)
> Uses `StreamingChatLanguageModel` — review streamed token by token

```
POST /code-review?language={language}
Content-Type: text/plain
Body: <your code>
```

| Parameter | Required | Default | Example |
|---|---|---|---|
| `language` | ❌ | `Java` | `Python`, `JavaScript`, `Go` |

**Example:**
```bash
curl -N -X POST "http://localhost:8080/code-review?language=Java" \
  -H "Content-Type: text/plain" \
  -d 'public int sum(int a, int b) { return a - b; }'
```

---

## 🐳 Docker Reference

### Rebuild after code changes

```bash
docker compose up --build ai-assistant
```

### View logs

```bash
docker compose logs -f ai-assistant
docker compose logs -f ollama
```

### Stop everything

```bash
docker compose down
```

### Remove model data (⚠️ re-download required)

```bash
docker compose down -v
```

---

## 📁 Project Structure

```
langchain4j-HelloProject/
├── docker-compose.yml
├── README.md
└── ai-assistant-backend/
    ├── Dockerfile
    ├── pom.xml
    └── src/main/java/com/hmayda/ai/assistant/
        ├── AiAssistantDemoApplication.java
        ├── controller/
        │   ├── ChatController.java        ← /recipe
        │   ├── StoryController.java       ← /story/stream
        │   ├── TravelController.java      ← /travel
        │   └── CodeReviewController.java  ← /code-review
        └── services/
            ├── aiServices/
            │   ├── Assistant.java             (@AiService)
            │   └── StreamingAssistant.java    (@AiService + TokenStream)
            └── chatServices/
                ├── ChatService.java           (ChatLanguageModel)
                └── StreamingChatService.java  (StreamingChatLanguageModel)
```

---

## 🔑 Key LangChain4j Concepts Demonstrated

**Declarative AI Services (`@AiService`)** — define AI behavior through annotations, no boilerplate:
```java
@AiService
public interface Assistant {
    @SystemMessage("You are an expert chef...")
    @UserMessage("Suggest a {{diet}} recipe using: {{ingredients}}")
    String suggestRecipe(@V("ingredients") String ingredients, @V("diet") String diet);
}
```

**Streaming with `TokenStream`** — real-time token-by-token responses over SSE:
```java
streamingAssistant.generateStory(genre, character, setting)
    .onNext(token -> emitter.send(token))
    .onComplete(r -> emitter.complete())
    .start();
```

---

## 👤 Author

**Abdo Essamad Hmayda** — [abdoessamadhmayda@gmail.com](mailto:abdoessamadhmayda@gmail.com)
