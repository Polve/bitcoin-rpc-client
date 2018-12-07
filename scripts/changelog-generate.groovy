#!/usr/bin/groovy

import java.net.URLEncoder;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.service.IssueService;
import org.eclipse.egit.github.core.service.MilestoneService;
import groovy.text.SimpleTemplateEngine;

/**
 * Generates project changelog markdown from GitHub issues
 */

// GitHub Services
def client = new GitHubClient()
def issueService = new IssueService(client)
def milestoneService = new MilestoneService(client)
def gitHubNamespace = properties.get('github.namespace')
def gitHubProject = properties.get('github.project')

// Issue categories that we're interested in
def issueCategories = ['feature', 'task', 'bug']

// Fetch and sort all closed milestones
def milestones = milestoneService.getMilestones(gitHubNamespace, gitHubProject, "closed")
milestones.sort{a,b-> b.title <=> a.title}

// Fetch closed issues for each milestone and sort into categories
def milestoneIssues = [:]
milestones.each { milestone ->
    def issueFilter = [:]
    issueFilter.put(IssueService.FILTER_STATE, "closed")
    issueFilter.put(IssueService.FILTER_MILESTONE, String.valueOf(milestone.getNumber()))
    issueFilter.put(IssueService.FIELD_SORT, IssueService.SORT_CREATED)
    issueFilter.put(IssueService.FIELD_DIRECTION, IssueService.DIRECTION_ASCENDING)

    def issues = issueService.getIssues(gitHubNamespace, gitHubProject, issueFilter)
    def issueMap = [:]
    issues.each { issue ->
        issue.labels.each { label ->
            if (issueCategories.contains(label.name.toLowerCase())) {
                if (issueMap[label.name] == null) {
                    issueMap[label.name] = []
                }
                issue.url = "https://github.com/${gitHubNamespace}/${gitHubProject}/issues/${issue.number}"
                issueMap[label.name] << issue
            }
        }
    }

    milestoneIssues[milestone] = issueMap
}

// Write content to Changelog.md
def binding = ["milestoneIssues" : milestoneIssues, "issueCategories" : issueCategories]
def engine = new SimpleTemplateEngine()
def template = engine.createTemplate(new File("${project.basedir}/scripts/changelog.template")).make(binding)

new File("${project.basedir}/Changelog.md").setText(template.toString().trim())
