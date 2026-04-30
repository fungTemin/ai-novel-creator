package com.novel

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

// AI 小说创作系统 — Spring Boot 应用入口
@SpringBootApplication
class NovelApplication

// 应用启动入口
fun main(args: Array<String>) {
    runApplication<NovelApplication>(*args)
}
