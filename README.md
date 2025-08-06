# AI Social Media Agent

A Spring AI application that integrates with local Ollama to create an intelligent agent capable of accessing and analyzing content from Chinese social media platforms: Bilibili, Douyin, and Weibo.

## 🚀 Features

- **Multi-Platform Support**: Access content from Bilibili, Douyin, and Weibo
- **AI-Powered Analysis**: Uses Ollama (local LLM) for intelligent content analysis
- **Comprehensive Insights**: Sentiment analysis, trend detection, engagement patterns
- **Content Recommendations**: AI-generated suggestions for content creators
- **Historical Tracking**: Store and analyze content over time
- **REST API**: Full RESTful API for programmatic access
- **Web Interface**: Beautiful, responsive web UI for easy interaction

## 🛠️ Technology Stack

- **Backend**: Spring Boot 3.2, Spring AI 0.8.1
- **AI/LLM**: Ollama (local deployment)
- **Database**: H2 (in-memory, easily configurable to other databases)
- **Web Client**: Spring WebFlux
- **Frontend**: HTML5, CSS3, Vanilla JavaScript
- **Data Processing**: Jackson, JSoup for HTML parsing

## 📋 Prerequisites

1. **Java 17+**
2. **Maven 3.6+**
3. **Ollama** installed and running locally
4. **Ollama Model**: Download a model (e.g., `ollama pull llama3.1`)

## ⚙️ Installation & Setup

### 1. Install Ollama

```bash
# On macOS
brew install ollama

# On Linux
curl -fsSL https://ollama.ai/install.sh | sh

# On Windows
# Download from https://ollama.ai/download
```

### 2. Start Ollama and Pull a Model

```bash
# Start Ollama service
ollama serve

# In another terminal, pull a model
ollama pull llama3.1
```

### 3. Clone and Run the Application

```bash
# Clone the repository
git clone <repository-url>
cd ai-social-agent

# Build and run
mvn clean install
mvn spring-boot:run
```

The application will start on `http://localhost:8080`

## 🎯 Usage

### Web Interface

1. Open `http://localhost:8080` in your browser
2. Select a platform (Bilibili, Douyin, or Weibo)
3. Enter blogger identifier:
   - **Bilibili**: User ID or `space.bilibili.com/[ID]`
   - **Douyin**: User URL or `@username`
   - **Weibo**: User ID or profile URL
4. Configure analysis parameters
5. Click "Analyze Content" to start

### API Endpoints

#### Analyze Blogger Content
```bash
POST /api/v1/agent/analyze
Content-Type: application/json

{
  "platform": "bilibili",
  "bloggerIdentifier": "123456789",
  "limit": 10,
  "includeAnalysis": true
}
```

#### Get Recommendations
```bash
POST /api/v1/agent/recommendations?platform=bilibili&bloggerName=ExampleUser
```

#### Get Historical Content
```bash
GET /api/v1/agent/history?platform=bilibili&bloggerName=ExampleUser
```

#### Get Recent Content
```bash
GET /api/v1/agent/recent?hours=24
```

#### Health Check
```bash
GET /api/v1/agent/health
```

#### Supported Platforms
```bash
GET /api/v1/agent/platforms
```

## 📊 Example Response

```json
{
  "platform": "bilibili",
  "bloggerName": "TechReviewer",
  "totalContents": 10,
  "contents": [
    {
      "id": 1,
      "platform": "bilibili",
      "bloggerName": "TechReviewer",
      "title": "Latest iPhone Review",
      "content": "In this video, I review the latest iPhone...",
      "contentUrl": "https://www.bilibili.com/video/BV1234567890",
      "likes": 1520,
      "comments": 89,
      "views": 15230,
      "publishTime": "2024-01-15T10:30:00",
      "aiAnalysis": "This content focuses on technology reviews with high engagement. The sentiment is positive with detailed analysis of product features..."
    }
  ],
  "overallAnalysis": "The blogger shows consistent performance in tech content with strong audience engagement. Recommended topics include emerging technologies and comparative reviews...",
  "processingTimeMs": 2340
}
```

## 🔧 Configuration

### Application Configuration (`application.yml`)

```yaml
spring:
  ai:
    ollama:
      base-url: http://localhost:11434
      chat:
        model: llama3.1
        options:
          temperature: 0.7
          num-ctx: 4096

social-media:
  bilibili:
    base-url: https://api.bilibili.com
  douyin:
    base-url: https://www.douyin.com
  weibo:
    base-url: https://m.weibo.cn

agent:
  max-content-length: 5000
  analysis-prompt: |
    You are an AI agent specialized in analyzing social media content...
```

### Custom Ollama Model

To use a different model:

1. Pull the model: `ollama pull [model-name]`
2. Update `application.yml`:
   ```yaml
   spring:
     ai:
       ollama:
         chat:
           model: [model-name]
   ```

## 🏗️ Architecture

```
┌─────────────────┐    ┌──────────────────┐    ┌─────────────────┐
│   Web UI        │    │   REST API       │    │   AI Analysis   │
│   (Static)      │────│   Controllers    │────│   Service       │
└─────────────────┘    └──────────────────┘    └─────────────────┘
                                │                        │
                                │                        │
                       ┌──────────────────┐    ┌─────────────────┐
                       │   Agent Service  │    │   Ollama        │
                       │   (Orchestrator) │────│   (Local LLM)   │
                       └──────────────────┘    └─────────────────┘
                                │
                    ┌───────────┼───────────┐
                    │           │           │
            ┌───────────┐ ┌──────────┐ ┌──────────┐
            │ Bilibili  │ │ Douyin   │ │ Weibo    │
            │ Service   │ │ Service  │ │ Service  │
            └───────────┘ └──────────┘ └──────────┘
                    │           │           │
            ┌───────────┐ ┌──────────┐ ┌──────────┐
            │ Bilibili  │ │ Douyin   │ │ Weibo    │
            │ API/Web   │ │ Web      │ │ API/Web  │
            └───────────┘ └──────────┘ └──────────┘
```

## 🔍 Platform-Specific Notes

### Bilibili
- Uses official API endpoints
- Requires user ID or space URL
- Provides rich metadata (views, likes, comments)
- Best data availability

### Douyin
- Web scraping approach (JavaScript-heavy site)
- Limited metadata due to anti-bot measures
- May require additional tools for full functionality
- Use @username or full user URL

### Weibo
- Mobile API endpoints for better access
- Good metadata availability
- Supports user ID or profile URL
- HTML content cleaning included

## 🚨 Important Considerations

1. **Rate Limiting**: Implement appropriate delays between requests
2. **Legal Compliance**: Ensure compliance with platform terms of service
3. **Data Privacy**: Handle user data responsibly
4. **Error Handling**: Robust error handling for network issues
5. **Scalability**: Consider database choice for production use

## 🛡️ Production Deployment

### Database Configuration

Replace H2 with a production database:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/social_agent
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
```

### Environment Variables

```bash
export OLLAMA_BASE_URL=http://your-ollama-server:11434
export OLLAMA_MODEL=llama3.1
export DB_USERNAME=your_db_user
export DB_PASSWORD=your_db_password
```

### Docker Deployment

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/ai-social-agent-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request

## 📜 License

This project is licensed under the MIT License - see the LICENSE file for details.

## 🙏 Acknowledgments

- Spring AI team for the excellent AI integration framework
- Ollama for providing local LLM capabilities
- Chinese social media platforms for their content ecosystems

## 📞 Support

For questions, issues, or contributions, please open an issue in the GitHub repository.