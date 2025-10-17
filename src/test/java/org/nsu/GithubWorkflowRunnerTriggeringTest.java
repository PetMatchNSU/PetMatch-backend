package org.nsu;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GithubWorkflowRunnerTriggeringTest {

    @Test
    void givenGithubWorkflowSelfRunner_whenPushCodeOnPullRequest_thenShouldTriggerTests() {
        assertEquals(1, 1);
    }
}