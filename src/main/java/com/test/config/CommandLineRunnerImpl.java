package com.test.config;

import com.test.task.SpecificTimeTask;
import com.test.utils.JsonUtil;
import org.springframework.boot.CommandLineRunner;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @ClassNme CommandLineRunnerImpl
 * @Description TODO
 * @Author chenpei
 * @Date 2025/01/26 12:32
 * @Version 1.0
 **/
@Component
public class CommandLineRunnerImpl implements CommandLineRunner {

    @Resource
    private RestTemplate restTemplate;

    @Resource
    private SpecificTimeTask specificTimeTask;

    @Override
    public void run(String... args) throws Exception {
        // サービス起動する時、curl -X 'POST' http://challenge.z2o.cloud/challenges?nickname=hogeをリクエストする
        // チャンレンジを作成します。
        String url = "http://challenge.z2o.cloud/challenges?nickname=cp38";
        System.out.println(url);
        HttpEntity<String> httpEntity = new HttpEntity<>(null, null);
        String result = restTemplate.postForObject(url, httpEntity, String.class);

        System.out.println(result);

        Map resultMap = JsonUtil.parse(result, Map.class);

        String challengeId = (String)resultMap.getOrDefault("id", "");
        //リスポンスによって、スレードプールを作成して、呼出予定時刻を次の実行時間としてを設定します。
        long actives_at = (long)resultMap.getOrDefault("actives_at", 0l);

        specificTimeTask.star(challengeId, actives_at);

    }
}
