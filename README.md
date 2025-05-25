# Task Timer

[![Build](https://github.com/Akhilesh170194/jb-plugin-task-timer/workflows/Build/badge.svg)](https://github.com/Akhilesh170194/jb-plugin-task-timer/actions)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

Task Timer helps developers track how much time is spent on specific tasks directly from within the IDE. The plugin adds a tool window for managing running tasks and stores the data using IntelliJ persistence APIs.

<!-- Plugin description -->
Task Timer helps you track the time spent on your development tasks directly in the IDE. Create tasks, start or pause timers, and maintain a full history of sessions for each project. Data is stored via IntelliJ persistent state components and can be exported for reporting.
<!-- Plugin description end -->

## Features

- Create, edit and delete tasks per project
- Start, pause and resume timers with optional automatic idle detection
- View session history and audit log for each task
- Notifications for idle pauses and long running tasks
- Export task information to CSV or JSON

## Installation

### Using the IDE built-in plugin system
1. Open **Settings/Preferences** &rarr; **Plugins** &rarr; **Marketplace**
2. Search for `jb-plugin-task-timer`
3. Click **Install**

### Using JetBrains Marketplace
1. Visit [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
2. Click **Install to ...** if your IDE is running, or download the latest release and install it from disk

### Manual download
Download the latest release from [GitHub Releases](https://github.com/Akhilesh170194/jb-plugin-task-timer/releases/latest) and install it via **Settings/Preferences** &rarr; **Plugins** &rarr; **⚙️** &rarr; **Install plugin from disk...**

## Usage

Open the **Task Timer** tool window from the bottom tool window bar. Create a new task and start or pause the timer as you work. A status bar widget shows the total time of all running tasks.

## Building from Source

```bash
./gradlew build
```

Run the plugin in a development IDE instance with:

```bash
./gradlew runIde
```

### Tests

Run the unit tests using JUnit:

```bash
./gradlew test --no-daemon
```

## Contributing

Contributions and bug reports are welcome! Feel free to open issues or submit pull requests.

## Changelog

See [CHANGELOG.md](CHANGELOG.md) for release notes.

---
Plugin based on the [IntelliJ Platform Plugin Template](https://github.com/JetBrains/intellij-platform-plugin-template).
