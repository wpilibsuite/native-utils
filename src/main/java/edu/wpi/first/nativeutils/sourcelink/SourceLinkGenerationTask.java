package edu.wpi.first.nativeutils.sourcelink;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import com.google.gson.GsonBuilder;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.submodule.SubmoduleStatus;
import org.eclipse.jgit.submodule.SubmoduleWalk;
import org.gradle.api.DefaultTask;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

public class SourceLinkGenerationTask extends DefaultTask {

    private final RegularFileProperty sourceLinkFile;
    private final MapProperty<String, String> gitRepos;

    @OutputFile
    public RegularFileProperty getSourceLinkBaseFile() {
        return sourceLinkFile;
    }

    @Input
    public MapProperty<String, String> getGitRepos() {
        return gitRepos;
    }

    private void resolveSubmodules(Repository repo) throws IOException, GitAPIException {
        // Current head
        String currentHead = repo.resolve(Constants.HEAD).getName();
        Config storedConfig = repo.getConfig();
        Set<String> remotes = repo.getRemoteNames();
        String remoteName = null;
        if (remotes.contains("origin")) {
            remoteName = "origin";
        } else {
            for (String rm : remotes) {
                remoteName = rm;
                break;
            }
        }

        // First remote
        String remoteUrl = storedConfig.getString("remote", remoteName, "url");
        String regexPattern = "^(?<url>(?<companyurl>(?:https:\\/\\/)?github\\.com\\/(?<company>[^\\/]+))\\/(?<project>[^\\/]+?)(\\.git)?)$";
        final Pattern pattern = Pattern.compile(regexPattern);
        Matcher matcher = pattern.matcher(remoteUrl);

        if (!matcher.find()) {
            return;
        }

        String company = matcher.group("company");
        String project = matcher.group("project");

        String remoteBase = "https://raw.github.com/" + company + "/" + project + "/" + currentHead + "/*";

        String localRoot = repo.getWorkTree() + File.separator + "*";
        getGitRepos().put(localRoot, remoteBase);

        try(Git git = Git.wrap(repo)) {
            Map<String, SubmoduleStatus> submodules = git.submoduleStatus().call();
            for (Entry<String, SubmoduleStatus> sm : submodules.entrySet()) {
                try (Repository subRepo = SubmoduleWalk.getSubmoduleRepository(repo, sm.getValue().getPath())) {
                    resolveSubmodules(subRepo);
                }
            }
        }


    }

    @Inject
    public SourceLinkGenerationTask(File gitDir) throws IOException, GitAPIException {
        ObjectFactory objects = getProject().getObjects();
        gitRepos = objects.mapProperty(String.class, String.class);
        sourceLinkFile = objects.fileProperty();
        sourceLinkFile.set(getProject().getLayout().getBuildDirectory().file("SourceLink.json"));

        try(Git git = Git.open(gitDir)) {
            Repository repo = git.getRepository();
            resolveSubmodules(repo);
        }
    }

    @TaskAction
    public void execute() throws IOException {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        SortedMap<String, String> sortedMap = new TreeMap<>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o2.length() - o1.length();
            }
        });
        for (Entry<String, String> elements : gitRepos.get().entrySet()) {
            sortedMap.put(elements.getKey(), elements.getValue());
        }
        String json = builder.create().toJson(sortedMap);
        List<String> jsonList = new ArrayList<>();
        jsonList.add(json);
        Files.write(sourceLinkFile.get().getAsFile().toPath(), jsonList, Charset.defaultCharset(), StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
    }
}
