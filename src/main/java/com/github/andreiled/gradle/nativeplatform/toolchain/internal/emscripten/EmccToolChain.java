package com.github.andreiled.gradle.nativeplatform.toolchain.internal.emscripten;

import org.gradle.api.internal.file.FileResolver;
import org.gradle.internal.operations.BuildOperationExecutor;
import org.gradle.internal.os.OperatingSystem;
import org.gradle.internal.reflect.Instantiator;
import org.gradle.internal.work.WorkerLeaseService;
import org.gradle.nativeplatform.internal.CompilerOutputFileNamingSchemeFactory;
import org.gradle.nativeplatform.toolchain.internal.gcc.AbstractGccCompatibleToolChain;
import org.gradle.nativeplatform.toolchain.internal.gcc.DefaultGccPlatformToolChain;
import org.gradle.nativeplatform.toolchain.internal.gcc.metadata.SystemLibraryDiscovery;
import org.gradle.nativeplatform.toolchain.internal.metadata.CompilerMetaDataProviderFactory;
import org.gradle.process.internal.ExecActionFactory;
import com.github.andreiled.gradle.nativeplatform.toolchain.Emcc;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.toolchain.internal.NativeLanguage;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

/**
 * The <a href="https://emscripten.org/index.html">Emscripten</a> tool chain implementation.
 */
@SuppressWarnings({"checkstyle:JavadocVariable", "checkstyle:MissingJavadocMethod"})
public class EmccToolChain extends AbstractGccCompatibleToolChain implements Emcc {

    public static final String DEFAULT_NAME = "emcc";

    @SuppressWarnings("checkstyle:ParameterNumber")
    public EmccToolChain(String name,
            BuildOperationExecutor buildOperationExecutor,
            OperatingSystem operatingSystem, FileResolver fileResolver,
            ExecActionFactory execActionFactory,
            CompilerOutputFileNamingSchemeFactory compilerOutputFileNamingSchemeFactory,
            CompilerMetaDataProviderFactory metaDataProviderFactory,
            SystemLibraryDiscovery standardLibraryDiscovery,
            Instantiator instantiator,
            WorkerLeaseService workerLeaseService) {
        super(name, buildOperationExecutor,
                operatingSystem, fileResolver,
                execActionFactory, compilerOutputFileNamingSchemeFactory,
                metaDataProviderFactory.clang(),
                standardLibraryDiscovery,
                instantiator, workerLeaseService);
    }

    @Override
    protected void configureDefaultTools(DefaultGccPlatformToolChain toolChain) {
        // clang++ => em++, clang => emcc
        toolChain.getLinker().setExecutable("em++.bat");
        toolChain.getcCompiler().setExecutable("emcc.bat");
        toolChain.getCppCompiler().setExecutable("em++.bat");
        toolChain.getObjcCompiler().setExecutable("emcc.bat");
        toolChain.getObjcppCompiler().setExecutable("em++.bat");
        toolChain.getAssembler().setExecutable("emcc.bat");
    }

    @Override
    protected String getTypeName() {
        return "Emscripten";
    }

    @Override
    public PlatformToolProvider select(NativeLanguage sourceLanguage, NativePlatformInternal targetMachine) {
        // TODO: ideally we need to override createPlatformToolProvider here, but its private
        PlatformToolProvider delegate = super.select(sourceLanguage, targetMachine);
        return (PlatformToolProvider) Proxy.newProxyInstance(delegate.getClass().getClassLoader(),
                new Class[]{PlatformToolProvider.class}, new InvocationHandler() {
            // Note: cannot use lambda here since the code below can throw checked exceptions resulting in UndeclaredThrowableException 
            @Override
            public Object invoke(Object object, Method method, Object[] params) throws Throwable {
                if ("getSharedLibraryName".equals(method.getName())) {
                    return params[0] + ".js";
                } else {
                    return method.invoke(delegate, params);
                }
            }
        });
    }
}
