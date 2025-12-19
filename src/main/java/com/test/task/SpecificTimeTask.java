package com.test.task;

import com.test.utils.JsonUtil;
import jakarta.annotation.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @ClassNme SpecificTimeTask
 * @Description TODO
 * @Author chenpei
 * @Date 2025/01/26 13:20
 * @Version 1.0
 **/
@Component
public class SpecificTimeTask {

    @Resource
    private RestTemplate restTemplate;

    public void star (String challengeId, long actives_at) {
        // 今の時間
        long currentTimeMillis = System.currentTimeMillis();
        // 遅延時間を計算
        long delay = actives_at - currentTimeMillis;

        if (delay < 0) {
            System.out.println("Specified time has already passed!");
            return;
        }
        // 创建一个单线程的调度线程池
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        scheduleTask(scheduler, challengeId, delay);

    }


    public void scheduleTask(ScheduledExecutorService scheduler, String challengeId, long initialDelay) {
        // タスクを始める
        scheduler.schedule(() -> {
            try {
                System.out.println("Task executed at: " + System.currentTimeMillis());
                HttpHeaders headers = new HttpHeaders();
                headers.add("X-Challenge-Id", challengeId);
                String url = "http://challenge.z2o.cloud/challenges";
                System.out.println(url);
                HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
                ResponseEntity<String> responseEntity = restTemplate.exchange(url, HttpMethod.PUT, httpEntity, String.class);
                System.out.println(responseEntity.getStatusCode());
                String result = responseEntity.getBody();
                System.out.println(result);
                Map resultMap = JsonUtil.parse(result, Map.class);
                // result はチャレンジ終了時のみレスポンスに含みます
                Map<String, String> result1 = (Map<String, String>) resultMap.get("result");
                if (Objects.nonNull(result1)) {
                    // 終了
                    System.out.println("Challenge has already finished!");
                    scheduler.shutdown();
                    return;
                }
                long actives_at = (long)resultMap.get("actives_at");
                // 今の時間
                long currentTimeMillis = System.currentTimeMillis();
                // 次の実行
                long nextDelay = actives_at - currentTimeMillis;
                if (nextDelay < 0) {
                    System.out.println("Specified time has already passed!");
                    scheduler.shutdown();
                    return;
                }
                scheduleTask(scheduler, challengeId, nextDelay);
            } catch (Exception e) {
                System.out.println("error !!!!!!!");
                System.out.println(e);
                throw new RuntimeException(e);
            }
        }, initialDelay, TimeUnit.MILLISECONDS);
    }
}
