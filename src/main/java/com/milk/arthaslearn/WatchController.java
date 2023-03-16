package com.milk.arthaslearn;

import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.milk.arthaslearn.dto.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "watch")
public class WatchController {

    @GetMapping(value = "test")
    public List<User> test(String method, String name2, @RequestBody List<User> userList) {
        if ("exception".equals(method)) {
            throw new RuntimeException("this way sir!");
        }
        if ("cost".equals(method)) {
            sleep3();
        }
        if ("target".equals(method)) {
            StaticClass.changeName2(name2);
        }
        if ("target2".equals(method)) {
            StaticClass.changeName3(name2);
        }
        if ("traceJdk".equals(method)) {
            System.out.println("sda".replace("s",""));
        }
        if ("deadLock".equals(method)) {
            deadLock();
        }
        return userList;
    }

    private void sleep3() {
        ThreadUtil.sleep(3000);
    }

    private void log() {
        log.info("controller log 被调用");
    }

    private void deadLock(){
        Object lock1 = new Object();
        Object lock2 = new Object();

        Thread thread1 = new Thread(new Runnable() {
            public void run() {
                synchronized (lock1) {
                    System.out.println("Thread 1: Holding Lock 1...");
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                    System.out.println("Thread 1: Waiting for Lock 2...");
                    synchronized (lock2) {
                        System.out.println("Thread 1: Holding Lock 1 & 2...");
                    }
                }
            }
        });
        thread1.setName("test-thread1");

        Thread thread2 = new Thread(new Runnable() {
            public void run() {
                synchronized (lock2) {
                    System.out.println("Thread 2: Holding Lock 2...");
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                    }
                    System.out.println("Thread 2: Waiting for Lock 1...");
                    synchronized (lock1) {
                        System.out.println("Thread 2: Holding Lock 1 & 2...");
                    }
                }
            }
        });
        thread2.setName("test-thread2");

        thread1.start();
        thread2.start();
    }
}
