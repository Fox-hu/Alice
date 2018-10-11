package com.xys

import com.xys.exten.ComExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class MainPlugin implements Plugin<Project> {

    String compilemodule = "app"

    @Override
    void apply(Project project) {

        System.out.println("========================")
        System.out.println("hello gradle plugin!")
        System.out.println("========================")

        project.extensions.create('combuild', ComExtension)

        String taskNames = project.gradle.startParameter.taskNames.toString()
        System.out.println("taskName is " + taskNames);

        String module = project.path.replace(":", "")
        System.out.println("current module is " + module)

        AssembleTask assembleTask = getTaskInfo(project.gradle.startParameter.taskNames)

        if (assembleTask.isAssemble) {
            fetchMainModuleName(project, assembleTask)
            System.out.println("compilemodule is " + compilemodule)
        }

        if (!project.hasProperty("isRunAlone")) {
            throw new RuntimeException("you should set isRunAlone in " + module + "'s gradle.properties")
        }

        boolean isRunAlone = Boolean.parseBoolean((project.properties.get("isRunAlone")))
        String mainmodulename = project.rootProject.property("mainmodulename")
        if (isRunAlone && assembleTask.isAssemble) {
            if (module.equals(compilemodule) || module.equals(mainmodulename)) {
                isRunAlone = true
            } else {
                isRunAlone = false
            }
        }
        project.setProperty("isRunAlone", isRunAlone)

        if (isRunAlone) {
            project.apply plugin: 'com.android.application'
            if (!module.equals(mainmodulename)) {
                project.android.sourceSets {
                    main {
                        manifest.srcFile 'src/main/runalone/AndroidManifest.xml'
                        java.srcDirs = ['src/main/java', 'src/main/runalone/java']
                        res.srcDirs = ['src/main/res', 'src/main/runalone/res']
                        assets.srcDirs = ['src/main/assets', 'src/main/runalone/assets']
                        jniLibs.srcDirs = ['src/main/jniLibs', 'src/main/runalone/jniLibs']
                    }
                }
            }
            System.out.println("apply plugin is " + 'com.android.application')
            if (assembleTask.isAssemble && module.equals(compilemodule)) {
                compileComponents(assembleTask, project)
                project.android.registerTransform(new ComCodeTransform(project))
            }
        } else {
            project.apply plugin: 'com.android.library'
            System.out.println("apply plugin is " + 'com.android.library')
        }
    }

    private void fetchMainModuleName(Project project, AssembleTask assembleTask) {
        if (!project.rootProject.hasProperty("mainmodulename")) {
            throw new RuntimeException("you should set compilemodule in rootproject's gradle.properties")
        }

        if (!assembleTask.modules.isEmpty() && !assembleTask.modules.get(0).equals("all")) {
            compilemodule = assembleTask.modules.get(0)
        } else {
            compilemodule = project.rootProject.property("mainmodulename")
        }

        if (compilemodule == null || compilemodule.trim().length() <= 0) {
            compilemodule = "app"
        }
    }

    private AssembleTask getTaskInfo(List<String> taskNames) {
        AssembleTask assembleTask = new AssembleTask()
        for (String task : taskNames) {
            if (task.toUpperCase().contains("ASSEMBLE")
                    || task.contains("aR")
                    || task.toUpperCase().contains("TINKER")
                    || task.toUpperCase().contains("INSTALL")
                    || task.toUpperCase().contains("RESGUARD")) {
                if (task.toUpperCase().contains("DEBUG")) {
                    assembleTask.isDebug = true
                }
                assembleTask.isAssemble = true
                String[] strs = task.split(":")
                assembleTask.modules.add(strs.length > 1 ? strs[strs.length - 2] : "all")
                break
            }
        }
        return assembleTask
    }

    private void compileComponents(AssembleTask assembleTask, Project project) {
        String components
        if (assembleTask.isDebug) {
            components = (String) project.properties.get("debugComponent")
        } else {
            components = (String) project.properties.get("compileComponent")
        }
        if (components == null || components.length() == 0) {
            System.out.println("there is no add dependencies ")
            return
        }

        String[] compileComponents = components.split(",")
        if (compileComponents == null || compileComponents.length == 0) {
            System.out.println("there is no add dependencies")
        }

        for (String str : compileComponents) {
            System.out.println("comp is " + str)
            if (str.contains(":")) {
                project.dependencies.add("compile", str)
                System.out.println("add dependencies lib : " + str)
            } else {
                project.dependencies.add("compile", project.project(':' + str))
                System.out.println("add dependencies project : " + str)
            }
        }
    }

    private class AssembleTask {
        boolean isAssemble = false
        boolean isDebug = false

        List<String> modules = new ArrayList<>()
    }
}