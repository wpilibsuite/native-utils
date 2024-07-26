package edu.wpi.first.deployutils.deploy.cache;

import org.codehaus.groovy.runtime.EncodingGroovyMethods;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import edu.wpi.first.deployutils.deploy.context.DeployContext;
import edu.wpi.first.deployutils.log.ETLogger;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import javax.inject.Inject;

public class Md5FileCacheMethod extends AbstractCacheMethod {
    private Logger log = Logging.getLogger(Md5SumCacheMethod.class);
    private int csI = 0;
    private Gson gson = new Gson();
    private Type mapType = new TypeToken<Map<String, String>>(){}.getType();

    @Inject
    public Md5FileCacheMethod(String name) {
        super(name);
    }

    @Override
    public boolean compatible(DeployContext context) {
        return true;
    }

    private Map<String, String> getRemoteCache(DeployContext ctx) {
        String remote_cache = ctx.execute("cat cache.md5 2> /dev/null || echo '{}'").getResult();
        return gson.fromJson(remote_cache, mapType);
    }

    public Map<String, String> localChecksumsMap(Map<String, File> files) {
        MessageDigest md;
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e1) {
            throw new RuntimeException(e1);
        }
        return files.entrySet().stream().collect(Collectors.toMap(entry -> {
            return entry.getKey();
        }, entry -> {
            md.reset();
            try {
                md.update(Files.readAllBytes(entry.getValue().toPath()));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
                return EncodingGroovyMethods.encodeHex(md.digest()).toString();
            }));
    }

    @Override
    public Set<String> needsUpdate(DeployContext context, Map<String, File> files) {
        ETLogger logger = context.getLogger();
        if (logger != null) {
            logger.silent(true);
        }
        int cs = csI++;
        log.debug("Comparing File Checksum " + cs + "...");

        Map<String, String> remote_md5 = getRemoteCache(context);

        if (log.isDebugEnabled()) {
            log.debug("Remote Cache " + cs + ":");
            log.debug(gson.toJson(remote_md5, mapType));
        }

        Map<String, String> local_md5 = localChecksumsMap(files);

        if (log.isDebugEnabled()) {
            log.debug("Local JSON Cache " + cs + ":");
            log.debug(gson.toJson(local_md5, mapType));
        }

        Set<String> needs_update = files.keySet().stream().filter(name -> {
            String md5 = remote_md5.get(name);
            return md5 == null || !md5.equals(local_md5.get(name));
        }).collect(Collectors.toSet());

        if (needs_update.size() > 0) {
            context.execute("echo '" + gson.toJson(local_md5, mapType) + "' > cache.md5");
        }

        if (logger != null) {
            logger.silent(false);
        }
        return needs_update;
    }
}
