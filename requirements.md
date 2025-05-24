# Project Task Timer Plugin – Requirements Document

## Overview

This JetBrains IDE plugin helps developers track time spent on tasks per project. It supports task creation, timer
management, automatic pausing/resuming based on IDE activity, persistent task history, and various export/configuration
options.

---

## Functional Requirements

### 1. Task Management

- Ability to create a task with:
    - Name (required)
    - Tag (optional)
    - Idle timeout override (optional)
    - Long task notification override (optional)
- View all tasks in a project-specific grid with columns:
    - Name
    - Status (Running / Paused / Stopped)
    - Running Time (formatted `HH:mm:ss`)
    - Start Time
    - Stop Time
    - Tag
    - Action buttons: Edit, Delete, Stop, Pause, Resume
- Audit/History of the task:
    - When a task is created
    - For any change in state or name
    - When automatically pauses or resumes
    - when a task is stopped

### 2. Timer Behavior

- Timer can be started or stopped manually per task
- Automatically pauses if:
    - IDE becomes idle for a configurable duration (default 5 minutes)
    - IDE is closed or loses focus
- Automatically resumes when IDE becomes active
- Per-task override of idle timeout duration

### 3. Notifications

- Displays balloon notifications for:
    - Auto pause on inactivity
    - Auto resume on activity
    - Long running tasks based on configured threshold per task

---

## Configuration Requirements

### 1. Global Settings (Application Scope)

Accessible under: `Preferences / Settings → Tools -> Task Timer`

- Default idle timeout (e.g. 5 minutes)
- Default long task duration threshold (e.g. 30 minutes)
- TimeZone to display any time in teh Grid (drop down to choose the timezone)

### 3. Per-Task Overrides

- While creating or editing a task, user can configure:
    - Idle timeout
    - Long running task Notification threshold
- These values apply only to that task and do not change global/project defaults

---

## Data Persistence

- Uses JetBrains `PersistentStateComponent` for:
    - Task state per project
    - Global settings
    - Project-specific settings
    - Session history logs per task

---

## Export Features

- Export task data to:
    - CSV
    - JSON
- Each exported record includes:
    - Task name, status, running time, timestamps, pause/resume counts, tag

---

## Advanced Features & Suggestions (Included)

- Session History: log every start/pause/resume timestamp per task
- Status Bar Widget: show live total time of running tasks
- Modern UI Design: clean grid layout, colorful icons, consistent dark theme
- Inline editing: planned for editable fields like tag or name
- Task filtering and sorting support

---

## Build & Run

- Uses official Gradle setup with:
    - `org.jetbrains.intellij` plugin
    - Compatible with IntelliJ Platform 2023.3+
- Run with:
  ```bash ./gradlew runIde```

---

## Detailed Explanation of the Task Feature

The core purpose of this plugin is to help developers stay aware of how much time they spend on specific tasks while
working within JetBrains IDEs (e.g., IntelliJ IDEA, PyCharm, WebStorm). This is particularly useful for:

- Personal productivity tracking
- Time logging for client work or agile task boards
- Reducing task context switching and increasing focus

Each **Task** represents a single unit of work. The user can start, pause, resume, or stop a task's timer manually.
Additionally, the timer integrates with IDE activity to automate this behavior.

### Task Lifecycle

1. **Create Task**: User gives a name (e.g., "Fix login bug"), optionally tags it (e.g., "Bug", "Urgent"), and can
   override timing configurations if needed.
2. **Start Timer**: Timer starts counting time. This is shown in the UI and the status bar.
3. **Pause/Resume**: Can be done manually or automatically via IDE activity detection.
4. **Stop Task**: Marks the task as inactive, locking its state.
5. **Edit/Delete**: Modify task metadata or remove a task.

---

## Visual Overview of UI Components

- **Task Grid**: A modern, dark-themed table listing all tasks for the current project. Each row shows:
    - Name
    - Current Status
    - Running time formatted (HH:mm:ss)
    - Timestamp when it started
    - Timestamp when it last stopped
    - Pause/resume count
    - Task tag
    - Action buttons

- **Create Task Dialog**:
    - Fields: name, tag, idle timeout (optional), long task threshold (optional)
    - Defaults pulled from global settings
    - Overrides stored per task if provided

- **Settings Page**:
    - **Global Scope**: Default settings (idle timeout, long task threshold)
    - **Project Scope**: Optional override panel for project-specific configurations
    - Does **not** affect already created tasks

- **Status Bar Widget**:
    - Displays total active running time across all tasks
    - Updated every second

---

## Use Cases

### Example 1: Developer in a Meeting

- Starts task: "Sprint Planning"
- Timer auto-pauses when they walk away from the computer (idle)
- Auto-resumes when they return
- Logs 42 minutes in total, exported to CSV

### Example 2: Deep Work Session

- Developer tags a task as “Focus”
- Sets long-task notification to 25 minutes
- Timer runs and notifies them to take a break

---

## Extensibility Suggestions (for future)

- Integration with task boards (e.g., Jira, Trello)
- Weekly summary view
- Pomodoro mode (25/5 min sessions)
- Git commit tagging with task ID