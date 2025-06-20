# Italian Checkers 🎯

A complete implementation of Italian Checkers (Dama Italiana) built in Java with JavaFX for the Software Engineering course.

## 📋 Features

- **3 Game Modes:**
    - **Local**: Two players on the same machine
    - **CPU**: Play against artificial intelligence
    - **Online**: Multiplayer over network

- **Complete Italian Checkers Rules:**
    - Mandatory capture
    - Multi-jump (consecutive multiple captures)
    - Piece promotion to king
    - 40-move rule without capture (draw)

- **Modern Graphical Interface:**
    - Timer for each player
    - Real-time score display
    - Animations and visual effects
    - Victory screen with game statistics

## 🚀 Getting Started

### Prerequisites

- Java 21 or higher
- Maven 3.6+
- JavaFX 21

### Installation

1. **Clone the repository:**
   ```bash
   git clone <repository-url>
   cd DamaProject
   ```

2. **Compile the project:**
   ```bash
   mvn clean compile
   ```

3. **Run tests:**
   ```bash
   mvn test
   ```

4. **Build executable JAR:**
   ```bash
   mvn package
   ```

## 🎮 How to Play

### Quick Start with GUI

```bash
mvn javafx:run
```

This will launch the main menu where you can choose your game mode.

### Running with JAR

```bash
java -jar target/DamaProject-1.0-SNAPSHOT-jar-with-dependencies.jar
```

### Specific Modes

#### Local Game
- Choose "Local" from the main menu
- Players take turns using the same computer

#### CPU Game
- Choose "CPU" from the main menu
- Play against the artificial intelligence

#### Online Game
1. **Start the server:**
   ```bash
   java -jar DamaProject-1.0-SNAPSHOT-jar-with-dependencies.jar -s
   ```

2. **Start clients:**
   ```bash
   java -jar DamaProject-1.0-SNAPSHOT-jar-with-dependencies.jar -c
   ```

## 🕹️ Controls

- **Left Click**: Select a piece
- **Drag & Drop**: Move the piece to desired position
- Highlighted cells show possible moves
- **Mandatory Capture**: You must capture when possible

## 🏗️ Architecture

```
src/
├── main/java/it/polimi/
│   ├── client/          # JavaFX client and interfaces
│   ├── server/          # Server and AI
│   ├── model/           # Game models (Piece, Tile, etc.)
│   └── common/          # Shared utilities
└── test/java/           # JUnit tests
```

### Main Components

- **ChessBoardClient**: Main graphical interface
- **Server**: Manages multiplayer games
- **CheckersAI**: Artificial intelligence for CPU mode
- **Piece**: Represents game pieces
- **Tile**: Represents board squares

## 🧪 Testing

The project includes comprehensive tests for all components:

```bash
# Run all tests
mvn test
```

Tests cover:
- Game logic
- Move validation
- Computer AI
- Coordinate conversions
- Error handling

## 🔧 Configuration

Main configurations are found in `GameConfig.java`:

- Board size: 8x8
- Tile size: 100px
- Server port: 1234
- Connection timeout: 5000ms

## 📦 Build and Deploy

### Creating JAR with dependencies

```bash
mvn clean package
```

The executable JAR will be in `target/DamaProject-1.0-SNAPSHOT-jar-with-dependencies.jar`

## 🤝 Contributing

1. Fork the project
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License. See the `LICENSE` file for details.

## 👥 Authors

- **Matrundola Tony David** - Main Developer

---

**Course**: Software Engineering  
**Year**: 2025  
**University**: Politecnico di Milano