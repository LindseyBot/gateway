package net.notfab.lindsey.framework;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class Utils {

    private static final ExecutorService timeoutService = Executors.newCachedThreadPool();

    public static <T> List<List<T>> chopped(List<T> list, final int L) {
        List<List<T>> parts = new ArrayList<>();
        final int N = list.size();
        for (int i = 0; i < N; i += L) {
            parts.add(new ArrayList<>(
                list.subList(i, Math.min(N, i + L)))
            );
        }
        return parts;
    }

    public static <T> T timeout(Callable<T> runnable, long time, TimeUnit unit) {
        RunnableFuture<T> future = new FutureTask<>(runnable);
        T result = null;
        try {
            timeoutService.execute(future);
            result = future.get(time, unit);
        } catch (TimeoutException ex) {
            // timed out. Try to stop the code if possible.
            future.cancel(true);
        } catch (InterruptedException | ExecutionException ignored) {
            // ignored
        }
        return result;
    }

}
