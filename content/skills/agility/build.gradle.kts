plugins {
    id("base-conventions")
    id("integration-test-suite")
}

dependencies {
    implementation(projects.api.pluginCommons)
    integrationImplementation(projects.api.generated)
    integrationImplementation(projects.api.hitPlugin)
    integrationImplementation(projects.api.objPlugin)
    integrationImplementation(libs.rsprot.api)
}
