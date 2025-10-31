package org.nsu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

// TODO: Удалить при наличии реальных тестов. Использовался для проверки работы Github Workflow.
class GithubWorkflowRunnerTriggeringTest {

    @Test
    void givenGithubWorkflowSelfRunner_whenPushCodeOnPullRequest_thenShouldTriggerTests() {
        assertEquals(1, 1);
    }
}