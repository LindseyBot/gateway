package net.notfab.lindsey.core.framework.actions;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.ContextFactory;
import org.mozilla.javascript.ScriptableObject;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.*;

@Service
public class ScriptingService {

    private static final long TIMEOUT_MILLIS = 1500;

    private final ExecutorService pool = Executors.newCachedThreadPool();

    /**
     * Used by eval command.
     *
     * @param timeout Script timeout.
     * @param script  Actual script to execute.
     * @param args    Map with all available params.
     * @return Result.
     * @throws ScriptError In case of error or timeout.
     */
    public Object unsafe(long timeout, String script, Map<String, Object> args) throws ScriptError {
        Callable<Object> callable = () -> ContextFactory.getGlobal().call(cx -> {
            ScriptableObject scope = cx.initSafeStandardObjects();
            args.forEach((name, value) -> {
                Object wrapped = Context.javaToJS(value, scope);
                ScriptableObject.putProperty(scope, name.toLowerCase(), wrapped);
            });
            return cx.evaluateString(scope, script, "unsafe", 1, null);
        });
        Future<Object> future = this.pool.submit(callable);
        try {
            return future.get(timeout, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new ScriptError(e, ScriptError.Type.INTERRUPTION);
        } catch (ExecutionException e) {
            throw new ScriptError(e, ScriptError.Type.EXCEPTION);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ScriptError(e, ScriptError.Type.TIMEOUT);
        }
    }

    /**
     * Used by ACTIONS.
     *
     * @param script Script to execute.
     * @param args   Arguments.
     * @return Result.
     * @throws ScriptError In case of error or timeout.
     */
    public Object safe(String script, Map<String, Object> args) throws ScriptError {
        Callable<Object> callable = () -> ContextFactory.getGlobal().call(cx -> {
            ScriptableObject scope = cx.initSafeStandardObjects();
            args.forEach((name, value) -> {
                Object wrapped = Context.javaToJS(value, scope);
                ScriptableObject.putProperty(scope, name.toLowerCase(), wrapped);
            });
            return cx.evaluateString(scope, script, "safe", 1, null);
        });
        Future<Object> future = this.pool.submit(callable);
        try {
            return future.get(TIMEOUT_MILLIS, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            throw new ScriptError(e, ScriptError.Type.INTERRUPTION);
        } catch (ExecutionException e) {
            throw new ScriptError(e, ScriptError.Type.EXCEPTION);
        } catch (TimeoutException e) {
            future.cancel(true);
            throw new ScriptError(e, ScriptError.Type.TIMEOUT);
        }
    }

}
