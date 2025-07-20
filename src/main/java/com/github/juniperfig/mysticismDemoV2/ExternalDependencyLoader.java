package com.github.juniperfig.mysticismDemoV2;

import io.papermc.paper.plugin.loader.PluginClasspathBuilder;
import io.papermc.paper.plugin.loader.PluginLoader;
import io.papermc.paper.plugin.loader.library.impl.MavenLibraryResolver;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.repository.RemoteRepository;

import java.util.List;
import java.util.stream.Stream;

// Loads dependencies from maven automatically when the server loads
// Useful for safely handling dependencies where we might have version clashing
@SuppressWarnings({"UnstableApiUsage", "unused"})
public class ExternalDependencyLoader implements PluginLoader {

    // A list of dependency artifacts to process
    private final List<Dependency> artifacts = Stream.of(
                    "org.jetbrains.kotlin:kotlin-stdlib:2.2.20-Beta1"
            )
            .map(DefaultArtifact::new)
            .map(a -> new Dependency(a, null))
            .toList();

    @Override
    public void classloader(PluginClasspathBuilder classpathBuilder) {
        var resolver = new MavenLibraryResolver();
        // Mirror Repo for MavenCentral
        String mavenCentral = "https://repo1.maven.org/maven2/";
        // Pulls the URL from the environment variable 'MavenMirrorURL'
        // otherwise uses the above default repo as a fallback
        String envVarUrl = System.getenv("MavenMirrorURL");
        resolver.addRepository(new RemoteRepository.Builder("mavenCentralMirror", "default", envVarUrl != null ? envVarUrl : mavenCentral).build());
        artifacts.forEach(resolver::addDependency);
        classpathBuilder.addLibrary(resolver);
    }
}
