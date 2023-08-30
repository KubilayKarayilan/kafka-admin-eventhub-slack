#!/usr/bin/env groovy
def config = [
    scriptVersion              : 'v7',
    iqOrganizationName         : 'Team AOS',
    iqBreakOnUnstable          : true,
    iqEmbedded                 : true,
    pipelineScript             : 'https://git.aurora.skead.no/scm/ao/aurora-pipeline-scripts.git',
    downstreamSystemtestJob    : [ branch: env.BRANCH_NAME ],
    javaVersion                : 17,
    nodeVersion                : '16',
    jiraFiksetIKomponentversjon: true,
    chatRoom                   : "#aos-notifications",
    compilePropertiesIq        : "-x test",
    versionStrategy            : [
        [branch: 'master', versionHint: '1']
    ]
]
fileLoader.withGit(config.pipelineScript, config.scriptVersion) {
  jenkinsfile = fileLoader.load('templates/leveransepakke')
}
jenkinsfile.gradle(config.scriptVersion, config)