package com.github.akhilesh170194.jbplugintasktimer.model

enum class TaskStatus {
    NOT_STARTED("Not Started"),
    RUNNING("Running"),
    PAUSED("Paused"),
    STOPPED("Stopped");

    private val displayName: String;

    constructor(displayName: String) {
        this.displayName = displayName
    }

    override fun toString(): String {
        return displayName
    }
}
