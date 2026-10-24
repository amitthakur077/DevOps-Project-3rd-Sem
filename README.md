# 📦 Inventory Management System (IMS)

> **DevOps Project — 3rd Semester | Chandigarh University**
> A Java-based Inventory Management System with a modern Web UI and fully automated CI/CD pipeline using Git, GitHub, Jenkins, Maven, and JUnit 5.

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange?style=flat-square&logo=java)](https://www.oracle.com/java/)
[![Maven](https://img.shields.io/badge/Maven-3.9-blue?style=flat-square&logo=apachemaven)](https://maven.apache.org/)
[![JUnit 5](https://img.shields.io/badge/JUnit-5.10-brightgreen?style=flat-square)](https://junit.org/junit5/)
[![Jenkins](https://img.shields.io/badge/Jenkins-CI%2FCD-red?style=flat-square&logo=jenkins)](https://www.jenkins.io/)
[![GitHub](https://img.shields.io/badge/GitHub-amitthakur077-black?style=flat-square&logo=github)](https://github.com/amitthakur077/DevOps-Project-3rd-Sem)

---

## 📌 What Is This Project?

The **Inventory Management System (IMS)** is a software project built to demonstrate **DevOps practices** in action. It has two parts:

1. **A Java Console Application** — A fully functional inventory manager you run in the terminal.
2. **A Web-Based Dashboard** — A premium dark-mode browser dashboard that does the same thing visually.

Both versions let you manage a product inventory: add products, track stock, search items, receive low-stock alerts, and more.

The real focus of this project is the **DevOps pipeline**:
- Every code change is **version-controlled with Git and pushed to GitHub**.
- **Jenkins** automatically picks up the code, compiles it, runs tests, and packages it into a runnable `.jar` file.
- **JUnit 5** ensures all business logic is tested automatically on every build.

---

## 🚀 What Does It Do?

| Feature | Description |
|---|---|
| ➕ **Add Product** | Register a product with ID, name, price, quantity, and low-stock threshold |
| 📋 **View Products** | See all products in a neatly formatted table |
| 🔍 **Search** | Find products by ID or name (case-insensitive) |
| 📊 **Update Stock** | Increase or decrease quantity; blocks negative stock |
| 🗑️ **Delete Product** | Remove a product with a confirmation step |
| ⚠️ **Low-Stock Alerts** | Auto-flags products at or below their threshold |
| 💰 **Inventory Value** | Calculates total monetary value of all stock |
| ✅ **Input Validation** | Rejects duplicate IDs, negative prices, empty names, etc. |

---

## 🧰 Technology Stack

| Tool | Purpose |
|---|---|
| **Java 21** | Core application logic |
| **Apache Maven 3.9** | Build automation, dependency management, packaging |
| **JUnit 5** | Automated unit testing (25 tests) |
| **Git + GitHub** | Version control and remote code hosting |
| **Jenkins** | Continuous Integration — auto build, test, package |
| **HTML / CSS / JS** | Web-based UI (localStorage for data persistence) |

---

## 📁 Project Structure

```
inventory-management-system/
│
├── 📄 pom.xml                         ← Maven build configuration
├── 📄 Jenkinsfile                     ← Jenkins CI/CD pipeline (7 stages)
├── 📄 .gitignore                      ← Excludes target/, IDE files, etc.
├── 📄 README.md                       ← You are here!
│
├── 📂 webapp/                         ← Web-based UI (open in any browser)
│   ├── index.html                     ← Main HTML dashboard
│   ├── style.css                      ← Premium dark-mode styling
│   └── app.js                         ← All logic (mirrors Java service layer)
│
└── 📂 src/
    ├── main/java/com/inventory/
    │   ├── Main.java                  ← Console app entry point & UI
    │   ├── Product.java               ← Product model with validation
    │   └── InventoryService.java      ← All CRUD, search, stock logic
    │
    └── test/java/com/inventory/
        └── InventoryServiceTest.java  ← 25 automated JUnit 5 tests
```

---

## ▶️ How to Run

### 🌐 Option 1 — Web Dashboard (Easiest, No Setup Needed)

Just open in any browser — Chrome, Edge, Firefox:

```
webapp/index.html
```

**Steps:**
1. Open **File Explorer** and go to the `webapp/` folder
2. **Double-click** `index.html`
3. The dashboard opens instantly in your browser ✅

> Data is saved in browser **localStorage** — it persists even after refreshing the page.

---

### ☕ Option 2 — Java Console App

Make sure **Java 21** and **Maven 3.9+** are installed.

**Step 1 — Build the project:**
```powershell
cd "e:\1_Chandigarh University\3rd Sem Work\3rd-Sem-PROJECTS\DevOps-Project\inventory-management-system"
mvn clean package
```

**Step 2 — Run the JAR:**
```powershell
java -jar target\inventory-management-system-1.0.0.jar
```

You will see an interactive menu:
```
==========================================================================
                   INVENTORY MANAGEMENT SYSTEM (IMS v1.0)
          Automated CI Pipeline with Git, Maven, JUnit 5 & Jenkins
==========================================================================
  [1] Add Product
  [2] View All Products
  [3] Search Product (by ID or Name)
  [4] Update Stock Quantity
  [5] Delete Product
  [6] View Low-Stock Alerts
  [7] Inventory Summary & Statistics
  [8] Exit
```

---

### 🧪 Option 3 — Run Tests Only

To run all 25 automated JUnit 5 unit tests:

```powershell
mvn test
```

Expected output:
```
[INFO] Tests run: 25, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

---

### 🤖 Option 4 — Jenkins CI Pipeline (Automated)

> Jenkins must be running locally at `http://localhost:8080`

1. Open **[http://localhost:8080/job/inventory-ci-pipeline/](http://localhost:8080/job/inventory-ci-pipeline/)**
2. Click **"Build Now"**
3. Watch all 7 stages execute automatically:

```
✅ Checkout  →  ✅ Verify  →  ✅ Clean  →  ✅ Compile
     →  ✅ Test (25/25 pass)  →  ✅ Package  →  ✅ Archive JAR
```

4. After build: Go to **Build Artifacts** to download `inventory-management-system-1.0.0.jar`

---

## 🔧 Maven Commands Reference

| Command | What It Does |
|---|---|
| `mvn clean` | Deletes old build output in `target/` |
| `mvn compile` | Compiles all Java source files |
| `mvn test` | Runs all 25 JUnit 5 unit tests |
| `mvn package` | Compiles + tests + creates the executable `.jar` |
| `mvn clean package` | Full clean build from scratch |

---

## ✅ Prerequisites

Make sure these are installed before building:

| Software | Version | Check Command |
|---|---|---|
| **JDK** | 21 or higher | `java -version` |
| **Maven** | 3.9 or higher | `mvn -version` |
| **Git** | Any recent | `git --version` |
| **Jenkins** | 2.440+ | Open `http://localhost:8080` |

### Environment Variables (Windows 11)
- `JAVA_HOME` → e.g. `C:\Program Files\Java\jdk-21`
- `MAVEN_HOME` → e.g. `E:\Software-Download\Apache Maven File\apache-maven-3.9.16`
- Both `bin/` folders must be in system `Path`

---

## 🔗 GitHub Repository

**[https://github.com/amitthakur077/DevOps-Project-3rd-Sem](https://github.com/amitthakur077/DevOps-Project-3rd-Sem)**

To push future changes:
```bash
git add .
git commit -m "your commit message"
git push origin main
```

---

## 🏗️ Jenkins Pipeline Stages

The `Jenkinsfile` defines 7 stages that run automatically:

| Stage | Maven Command | Purpose |
|---|---|---|
| 1. Checkout | — | Pull latest code from GitHub |
| 2. Verify | `java -version` | Check tools are available |
| 3. Clean | `mvn clean` | Remove old build files |
| 4. Compile | `mvn compile` | Build `.class` files |
| 5. Test | `mvn test` | Run 25 JUnit 5 tests |
| 6. Package | `mvn package` | Create executable `.jar` |
| 7. Archive | — | Store `.jar` in Jenkins |

---

## 🧪 Unit Tests Coverage

Tests are in `InventoryServiceTest.java` and cover:

- ✅ Adding products successfully
- ✅ Rejecting duplicate product IDs
- ✅ Rejecting negative prices, quantities, and thresholds
- ✅ Rejecting empty product names and IDs
- ✅ Searching by exact ID and name substring
- ✅ Updating stock (increase and decrease)
- ✅ Blocking stock reductions that cause negative inventory
- ✅ Detecting products below low-stock threshold
- ✅ Deleting products and handling missing IDs
- ✅ Calculating total inventory monetary value

---

## 💡 Sample Application Output (Console)

```
>>> CURRENT PRODUCT INVENTORY
+--------+--------------------------------+------------+----------+-----------+--------------+
| ID     | Name                           | Price ($)  | Quantity | Threshold | Stock Status |
+--------+--------------------------------+------------+----------+-----------+--------------+
| P101   | Dell XPS 15 Laptop             |    1499.99 |       12 |         5 | OK           |
| P102   | Logitech MX Master 3S Mouse    |      99.50 |        4 |        10 | LOW STOCK (!) |
| P103   | Mechanical RGB Keyboard        |      79.99 |       25 |         8 | OK           |
| P104   | USB-C Multiport Hub            |      34.50 |        6 |        15 | LOW STOCK (!) |
+--------+--------------------------------+------------+----------+-----------+--------------+
```

---

## 🛠️ Common Errors & Fixes

| Error | Fix |
|---|---|
| `'mvn' is not recognized` | Add Maven `bin/` to system `Path` environment variable |
| `java: release version 21 not supported` | Set `JAVA_HOME` to JDK 21 or higher |
| `sh: not found` on Jenkins Windows | Pipeline uses `bat` on Windows automatically — already handled |
| Git push asks for password | Use a GitHub Personal Access Token (PAT) instead of your password |
| JAR not found in `target/` | Run `mvn clean package` first |

---

## 👤 Author

**Amit Thakur**
- GitHub: [@amitthakur077](https://github.com/amitthakur077)
- Institution: Chandigarh University — 3rd Semester
- Course Focus: DevOps, CI/CD, Java Development, Automated Testing
