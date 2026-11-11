plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    implementation(projects.content.quest)
    integrationImplementation(projects.api.generated)
    integrationImplementation(projects.api.hitPlugin)
    integrationImplementation(projects.api.objPlugin)
    integrationImplementation(libs.rsprot.api)
}
