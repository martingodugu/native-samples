package org.gradle.samples

import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.FileCollection
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFiles
import org.gradle.api.tasks.TaskAction

/**
 * Task types to execute CMake
 */
class CMake extends DefaultTask {
    @Input String buildType
    @Internal final DirectoryProperty variantDir
    @Internal final DirectoryProperty projectDirectory
    @InputFiles ConfigurableFileCollection includeDirs
    @InputFiles ConfigurableFileCollection linkFiles


    CMake() {
        variantDir = newOutputDirectory()
        projectDirectory = newInputDirectory()

        includeDirs = project.files()
        linkFiles = project.files()
    }

    @TaskAction
    void generateCmakeFiles() {
        def cmakeExecutable = System.getenv('CMAKE_EXECUTABLE') ?: 'cmake'

        variantDir.get().asFile.mkdirs()
        project.exec {
            workingDir variantDir.get()
            commandLine cmakeExecutable,
                    "-DCMAKE_BUILD_TYPE=${buildType.capitalize()}",
                    "-DINCLUDE_DIRS=${includeDirs.join(';  ')}",
                    "-DLINK_DIRS=${linkFiles.collect { it.parent }.join(';')}",
                    "--no-warn-unused-cli",
                    projectDirectory.get().asFile.absolutePath
        }
    }

    @InputFiles
    FileCollection getCMakeLists() {
        return project.fileTree(projectDirectory.get().asFile).include('**/CMakeLists.txt')
    }

    @OutputFiles
    FileCollection getCmakeFiles() {
        project.fileTree(variantDir.get())
            .include('**/CMakeFiles/**/*')
            .include('**/Makefile')
            .include('**/*.cmake')
    }
}
