package com.github.andreiled.gradle.nativeplatform.toolchain.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.internal.file.FileResolver;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.service.ServiceRegistry;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.model.Defaults;
import org.gradle.model.RuleSource;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.plugins.NativeComponentPlugin;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainRegistryInternal;
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery;
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory;
import org.gradle.process.internal.ExecActionFactory;
import com.github.andreiled.gradle.nativeplatform.toolchain.Emcc;
import com.github.andreiled.gradle.nativeplatform.toolchain.internal.emscripten.EmccToolChain;

/**
 * A {@link Plugin} which makes the <a href="https://emscripten.org/index.html">Emscripten</a>
 * compiler available for compiling C/C++ code.
 */
public class EmccCompilerPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(NativeComponentPlugin.class);
    }

    static class Rules extends RuleSource {

        @Defaults
        @SuppressWarnings("checkstyle:LineLength")
        public static void addToolChain(NativeToolChainRegistryInternal toolChainRegistry, ServiceRegistry serviceRegistry) {
            final FileResolver fileResolver = serviceRegistry.get(FileResolver.class);
            final ExecActionFactory execActionFactory = serviceRegistry.get(ExecActionFactory.class);
            final CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory
                    = serviceRegistry.get(CompilerOutputFileNamingSchemeFactory.class);
            final Instantiator instantiator = serviceRegistry.get(Instantiator.class);
            final BuildOperationExecutor buildOperationExecutor = serviceRegistry.get(BuildOperationExecutor.class);
            final CompilerMetaDataProviderFactory metaDataProviderFactory = serviceRegistry.get(CompilerMetaDataProviderFactory.class);
            final SystemLibraryDiscovery standardLibraryDiscovery = serviceRegistry.get(SystemLibraryDiscovery.class);
            final WorkerLeaseService workerLeaseService = serviceRegistry.get(WorkerLeaseService.class);

            toolChainRegistry.registerFactory(Emcc.class, (String name) -> instantiator.newInstance(
                    EmccToolChain.class, name,
                    buildOperationExecutor, OperatingSystem.current(), fileResolver,
                    execActionFactory, compilerOutputFileNamingSchemeFactory,
                    metaDataProviderFactory, standardLibraryDiscovery,
                    instantiator, workerLeaseService));
            toolChainRegistry.registerDefaultToolChain(EmccToolChain.DEFAULT_NAME, Emcc.class);
        }
    }
}
