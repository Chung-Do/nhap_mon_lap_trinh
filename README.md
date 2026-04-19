# Remote Control Application - Project Nhập Môn Lập Trình

> A remote desktop control application for Windows machines on LAN using Java, TCP/IP sockets, and JSON messaging.

## 📚 Overview

This is a **learning project** demonstrating computer network concepts through building a remote control application. The project consists of:

- **Client Application**: GUI to control a remote machine
- **Server Application**: Executes commands from client on a remote machine

Both communicate via TCP/IP sockets using JSON message format over LAN (Local Area Network).

---

## ✨ Features

### Core Features
- ✅ List/Start/Stop Applications
- ✅ List/Start/Stop/Kill Processes  
- ✅ Screenshot Capture
- ✅ Keylogger
- ✅ File Transfer (Upload/Download)
- ✅ System Control (Shutdown/Restart)
- ✅ Webcam Stream
- ✅ Network Monitoring

### Advanced Features
- ✅ Remote Desktop
- ✅ System Lock (freeze input)
- ✅ Network Performance Metrics (bandwidth, latency)
- ✅ Real-time logging

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────┬─────────────────┐
│                   CLIENT MACHINE                    │  SERVER MACHINE │
├─────────────────────────────────────────────────────┼─────────────────┤
│                                                     │                 │
│  ┌─────────────────────────────────────┐           │                 │
│  │   ClientUI (Swing)                  │           │                 │
│  │  ┌─ Application Tab                 │           │                 │
│  │  ├─ Process Tab                     │           │                 │
│  │  ├─ Screenshot Tab                  │           │                 │
│  │  ├─ Keylogger Tab                   │           │                 │
│  │  ├─ File Transfer Tab               │           │                 │
│  │  ├─ System Control Tab              │           │                 │
│  │  ├─ Webcam Tab                      │           │                 │
│  │  ├─ Network Monitor Tab             │           │                 │
│  │  ├─ Remote Desktop Tab              │           │                 │
│  │  ├─ System Lock Tab                 │           │                 │
│  │  └─ Log Window                      │           │                 │
│  └─────────────────────────────────────┘           │                 │
│           │                                        │                 │
│           ↓                                        │                 │
│  ┌──────────────────────────────────┐             │                 │
│  │   ClientCore (Socket Connection) │             │                 │
│  └──────────────────────────────────┘             │                 │
│           │                                        │                 │
│           ↓                                        │                 │
│  ────────────[TCP Socket - Port 8888]──────────↓  │                 │
│           ↑                                        │    ┌────────────┴──────┐
│           │                         Connection    │    │                   │
│           └────────────────────────────────────────│──→ │ ServerCore       │
│                                                     │    │ (Accept)         │
│                                                     │    └─────────┬────────┘
│                                                     │            │
│                                                     │    ┌───────┴─────────┐
│                                                     │    │ MessageRouter   │
│                                                     │    └───────┬─────────┘
│                                                     │            │
│                                                     │    ┌───────┴─────────────────────┐
│                                                     │    │                             │
│                                                     │  Handlers:                      │
│                                                     │  ├─ ApplicationHandler          │
│                                                     │  ├─ ProcessHandler              │
│                                                     │  ├─ ScreenCaptureHandler       │
│                                                     │  ├─ KeyloggerHandler           │
│                                                     │  ├─ FileTransferHandler        │
│                                                     │  ├─ SystemControlHandler       │
│                                                     │  ├─ WebcamHandler              │
│                                                     │  ├─ NetworkMonitorHandler      │
│                                                     │  ├─ RemoteDesktopHandler       │
│                                                     │  └─ SystemLockHandler          │
│                                                     │                                 │
└─────────────────────────────────────────────────────┴─────────────────────────────────┘
```

---

## 🔌 Protocol & Messaging

### Connection
- **Protocol**: TCP/IP (Transmission Control Protocol)
- **Port**: 8888 (configurable)
- **Connection Type**: Persistent, full-duplex
- **Format**: Length-prefix framing + JSON

### Message Structure

```json
{
  "type": "REQUEST|RESPONSE|STREAM|ERROR|HEARTBEAT",
  "action": "LIST_APPS|SCREENSHOT|KILL_PROCESS|...",
  "requestId": "uuid-string",
  "timestamp": 1234567890,
  "data": { ... }
}
```

### Protocol Stack

```
┌──────────────────────────────┐
│  Application Layer           │  JSON Messages
│  (Commands & Data)           │
├──────────────────────────────┤
│  Length-Prefix Framing       │  [4 bytes length][N bytes data]
├──────────────────────────────┤
│  TCP Socket Layer            │  Reliable, ordered, 8888
├──────────────────────────────┤
│  IP Layer                    │  Routing within LAN
├──────────────────────────────┤
│  Link Layer                  │  Ethernet/WiFi
└──────────────────────────────┘
```

---

## 📦 Project Structure

```
nhap_mon_lap_trinh/
├── src/
│   ├── common/                           # Shared code
│   │   ├── Message.java                  # Message class
│   │   ├── MessageType.java              # Message types enum
│   │   ├── ActionType.java               # Action types enum
│   │   ├── Constants.java                # Global constants
│   │   ├── LogManager.java               # Logging utilities
│   │   └── NetworkUtils.java             # Network utilities
│   │
│   ├── client/                           # Client application
│   │   ├── ClientMain.java               # Entry point
│   │   ├── ClientCore.java               # Socket connection
│   │   ├── controllers/                  # Business logic (10 controllers)
│   │   └── ui/
│   │       ├── ClientUI.java             # Main window
│   │       └── tabs/                     # 10 tabs (1 per feature)
│   │
│   └── server/                           # Server application
│       ├── ServerMain.java               # Entry point
│       ├── ServerCore.java               # Socket listener
│       ├── MessageRouter.java            # Route messages to handlers
│       ├── handlers/                     # 10 feature handlers
│       └── ui/
│           └── ServerUI.java             # UI + logging
│
├── .github/
│   └── workflows/
│       └── build-and-package.yml         # GitHub Actions CI/CD
│
├── scripts/
│   ├── package.ps1                       # PowerShell packaging script
│   ├── package.bat                       # Batch packaging script
│   └── package.sh                        # Bash packaging script
│
├── build.bat & build.sh                  # Quick build commands
├── pom.xml                               # Maven configuration
├── BUILD_GUIDE.md                        # Build instructions
├── GITHUB_ACTIONS_GUIDE.md               # CI/CD documentation
└── README.md                             # This file
```

---

## 🚀 Quick Start

### Prerequisites
- **Java 11+** (Download: https://adoptium.net/)
- **Maven 3.6+** (Download: https://maven.apache.org/)
- **Windows OS** (some features are Windows-specific)
- **Network**: Both machines on same LAN

### Build Locally

**Windows:**
```batch
.\build.bat
```

**Mac/Linux:**
```bash
bash build.sh
```

This will:
1. ✅ Compile source code with Maven
2. ✅ Create `remote-client.jar` and `remote-server.jar`
3. ✅ Package into `remote-client.zip` and `remote-server.zip`

### Run Application

**Extract ZIP files and run:**

```bash
# Client
java -jar remote-client.jar

# Server
java -jar remote-server.jar
```

**Or use provided launchers:**
```batch
# Windows
cd remote-client
run.bat

cd remote-server
run.bat
```

### Configure Connection

1. In Client: Edit `config.properties`
   ```properties
   server.host=192.168.1.100    # Server IP on LAN
   server.port=8888              # Server port
   ```

2. Start Server first
3. Start Client and connect

---

## 🔨 Build System

### Maven
- **Language**: Java 11
- **Build Tool**: Apache Maven
- **Plugins Used**:
  - `maven-compiler-plugin`: Compile source code
  - `maven-assembly-plugin`: Create JARs with dependencies

### Output
- `target/remote-client.jar`: Client executable (~50MB with dependencies)
- `target/remote-server.jar`: Server executable (~50MB with dependencies)

---

## 🤖 Continuous Integration / Continuous Deployment

GitHub Actions automatically builds and packages on every push:

### Triggers
- ✅ Push to `main`, `master`, or `develop` branches
- ✅ Pull Requests
- ✅ Manual trigger (GitHub Actions tab)

### Artifacts
Download from GitHub Actions → Artifacts:
- `remote-client-package` (ZIP)
- `remote-server-package` (ZIP)

See [GITHUB_ACTIONS_GUIDE.md](GITHUB_ACTIONS_GUIDE.md) for details.

---

## 📚 Learning Outcomes

This project teaches:

1. **Network Programming**
   - TCP/IP socket communication
   - Client-server architecture
   - Protocol design (Length-prefix framing)
   - Message serialization (JSON)

2. **Java Concepts**
   - Object-oriented design
   - Multi-threading (ExecutorService)
   - Exception handling
   - File I/O
   - Java Swing GUI

3. **Software Engineering**
   - Project structure & organization
   - Documentation
   - Build automation (Maven)
   - CI/CD (GitHub Actions)
   - Version control (Git)

4. **System Integration**
   - Process management (Windows)
   - Screenshot capture
   - Keyboard/mouse input handling
   - Webcam integration
   - Registry access

---

## 🔐 Security Notes

⚠️ **This is a learning project, not production-ready!**

**Security considerations:**
- ❌ No encryption (data sent in clear text)
- ❌ No authentication (anyone can connect)
- ❌ No authorization (no role-based access)
- ❌ Vulnerable to man-in-the-middle attacks

**For production:**
- ✅ Add SSL/TLS encryption
- ✅ Implement authentication (username/password or certificates)
- ✅ Add authorization (role-based control)
- ✅ Rate limiting
- ✅ Input validation & sanitization
- ✅ Audit logging

---

## 🐛 Troubleshooting

### "Java not found"
→ Install Java 11+ from https://adoptium.net/

### "Maven not found"
→ Install Maven from https://maven.apache.org/

### Build fails
→ Check [BUILD_GUIDE.md](BUILD_GUIDE.md) troubleshooting section

### Cannot connect client to server
→ Ensure:
- Server is running
- Both machines on same LAN
- Firewall allows port 8888
- Client configured with correct server IP

### GUI is not responsive
→ Check if server is running and responding
→ Look at logs for errors

---

## 📖 Documentation

- **[BUILD_GUIDE.md](BUILD_GUIDE.md)**: Detailed build instructions
- **[GITHUB_ACTIONS_GUIDE.md](GITHUB_ACTIONS_GUIDE.md)**: GitHub Actions CI/CD setup
- **[log.md](log.md)**: Original project requirements & discussion

---

## 🎓 Author Notes

*Project for "Nhập Môn Lập Trình" (Introduction to Programming) course*

**Goal**: Build a remote control application to understand:
- Network protocols (TCP/IP)
- Client-server architecture  
- Multi-threading & concurrency
- Message protocol design
- Software project organization

**Constraints**:
- Keep it simple for learning
- Well-documented code
- Minimal dependencies
- Cross-platform where possible

---

## 📈 Future Enhancements

Possible extensions (beyond scope of current project):

- [ ] Encryption (SSL/TLS)
- [ ] Authentication (login)
- [ ] Authorization (roles & permissions)
- [ ] NAT traversal (control over Internet)
- [ ] Multi-client server (handle multiple clients)
- [ ] Cross-platform support (Linux, Mac)
- [ ] Docker containerization
- [ ] REST API alternative
- [ ] Mobile app (Android/iOS)
- [ ] Cloud integration

---

## 📞 Support

For questions or issues:
1. Check [BUILD_GUIDE.md](BUILD_GUIDE.md)
2. Check [GITHUB_ACTIONS_GUIDE.md](GITHUB_ACTIONS_GUIDE.md)
3. See original discussion in [log.md](log.md)

---

## ⚙️ Tech Stack

| Component | Technology |
|-----------|-----------|
| Language | Java 11 |
| Build Tool | Apache Maven |
| Network | TCP/IP Sockets |
| Messaging | JSON (org.json library) |
| GUI | Java Swing |
| CI/CD | GitHub Actions |
| Version Control | Git |
| Platform | Windows (Primary) |

---

## 📄 License

*Educational project - Use for learning purposes*

---

**Last Updated**: 2026-04-19  
**Status**: ✅ Active Development

