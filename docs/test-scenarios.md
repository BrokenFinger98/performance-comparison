# Performance Test Scenarios

## 목표

Spring MVC vs WebFlux + Coroutine의 성능 차이를 다양한 기술 스택 조합에서 실측하여, **어떤 상황에서 WebFlux가 유리하고 어떤 상황에서 차이가 없는지** 데이터로 증명한다.

---

## 공통 테스트 조건

| 항목 | 값 |
|------|-----|
| **Number of Threads** | 500 |
| **Ramp-Up Period** | 30s |
| **Duration** | 60s |
| **Simulated Delay** | 200ms |
| **JVM Heap** | `-Xms512m -Xmx512m` |
| **JDK** | 21 |
| **Spring Boot** | 3.4.1 |

### API Endpoints (모든 Round 공통)

| Method | Endpoint | Body (POST) |
|--------|----------|-------------|
| GET | /api/users | - |
| GET | /api/users/{id} | - |
| POST | /api/users | `{"name":"...","email":"...","age":...}` |

### Delay 구현 방식

| Stack | 방식 | 특성 |
|-------|------|------|
| MVC (블로킹) | `Thread.sleep(200ms)` | 스레드 점유 |
| MVC + Coroutine | `suspend fun` + `Thread.sleep(200ms)` | 블로킹 드라이버라 스레드 점유 동일 |
| WebFlux + Coroutine | `kotlinx.coroutines.delay(200ms)` | 스레드 해방 (논블로킹) |

---

## Round 1: MongoDB — MVC vs WebFlux + Coroutine

> 가장 기본적인 비교. 동일 DB(MongoDB)에서 블로킹 vs 논블로킹 전체 스택 차이를 측정.

| 항목 | MVC | WebFlux |
|------|-----|---------|
| Framework | Spring MVC (Tomcat) | Spring WebFlux (Netty) |
| DB Driver | MongoTemplate (블로킹) | ReactiveMongoTemplate + CoroutineCrudRepository (논블로킹) |
| Delay | `Thread.sleep(200ms)` | `kotlinx.coroutines.delay(200ms)` |
| Port | 8080 | 8081 |

**예상 결과:**
- MVC: Tomcat 스레드풀(200) 포화 → 나머지 300 요청 큐 대기 → 처리량 저하, 레이턴시 증가
- WebFlux: Netty 이벤트 루프 + 코루틴으로 스레드 제약 없이 처리 → 높은 처리량, 안정적 레이턴시

**상태: 완료**

---

## Round 2: MongoDB — MVC vs MVC + Coroutine

> MVC에서 Coroutine을 쓰면 이점이 있는지 확인. 블로킹 드라이버에서는 suspend fun을 써도 스레드가 점유되므로 이론상 이점 없음.

| 항목 | MVC | MVC + Coroutine |
|------|-----|-----------------|
| Framework | Spring MVC (Tomcat) | Spring MVC (Tomcat) |
| DB Driver | MongoTemplate (블로킹) | MongoTemplate (블로킹) |
| Controller | 일반 fun | suspend fun |
| Delay | `Thread.sleep(200ms)` | `Thread.sleep(200ms)` (블로킹 유지) |
| Port | 8080 | 8081 |

**예상 결과:**
- 차이 거의 없음 (블로킹 드라이버라 suspend 붙여도 스레드 점유)
- **"MVC에서 코루틴 쓰면 이점 없다"를 데이터로 증명**

**상태: 미진행**

---

## Round 3: MySQL — MVC + JPA vs WebFlux + R2DBC + Coroutine

> RDB 환경에서의 비교. 블로킹(JPA + HikariCP) vs 논블로킹(R2DBC + Coroutine) 전체 스택 차이를 측정.

| 항목 | MVC | WebFlux |
|------|-----|---------|
| Framework | Spring MVC (Tomcat) | Spring WebFlux (Netty) |
| DB Driver | JPA + HikariCP (블로킹) | R2DBC + Coroutine (논블로킹) |
| Delay | `Thread.sleep(200ms)` | `kotlinx.coroutines.delay(200ms)` |
| Port | 8080 | 8081 |

**예상 결과:**
- Round 1과 유사한 패턴
- MVC: 스레드풀 포화 + HikariCP 커넥션풀 병목 가능
- WebFlux: R2DBC 논블로킹 커넥션으로 안정적 처리

**상태: 미진행**

---

## Round 4: 네트워크 지연 시뮬레이션

> 실제 운영 환경처럼 DB가 다른 네트워크에 있는 상황. 네트워크 지연이 추가되면 블로킹 스레드 점유 시간이 길어져 차이가 극대화됨.

| 항목 | 설명 |
|------|------|
| 기반 | Round 1 또는 Round 3 구성 |
| 지연 주입 | toxiproxy 또는 `tc` 명령어 |
| 추가 지연 | 50ms, 100ms (app delay 200ms + 네트워크 지연) |

### 방법 A: Docker 네트워크 지연 주입 (tc)

```bash
docker exec perf-mongodb tc qdisc add dev eth0 root netem delay 50ms
```

### 방법 B: toxiproxy

```yaml
toxiproxy:
  image: ghcr.io/shopify/toxiproxy:latest
  ports:
    - "8474:8474"
    - "27018:27018"
```

**예상 결과:**
- 네트워크 지연이 클수록 WebFlux 우위 극대화
- MVC는 스레드가 네트워크 응답 대기하며 점유
- WebFlux는 코루틴 중단으로 스레드 해방

**상태: 미진행**

---

## 측정 항목 (모든 Round 공통)

### JMeter

| 항목 | 설명 |
|------|------|
| Throughput | 초당 처리 요청 수 (req/s) |
| Average Latency | 평균 응답 시간 (ms) |
| p50 Latency | 50번째 백분위 응답 시간 |
| p95 Latency | 95번째 백분위 응답 시간 |
| p99 Latency | 99번째 백분위 응답 시간 |
| Error Rate | 에러 비율 (%) |

### Grafana (Prometheus)

| 항목 | PromQL |
|------|--------|
| HTTP Request Rate | `sum(rate(http_server_requests_seconds_count{application=~"$app"}[1m])) by (application)` |
| JVM Heap Used | `sum(jvm_memory_used_bytes{application=~"$app", area="heap"}) by (application)` |
| Thread Count | `jvm_threads_live_threads{application=~"$app"}` |
| CPU Usage | `process_cpu_usage{application=~"$app"} * 100` |
| GC Pause | `rate(jvm_gc_pause_seconds_sum{application=~"$app"}[1m])` |

---

## 결과 파일 명명 규칙

```
{round}-{stack}-{db}-delay{ms}-t{threads}.csv

예시:
round1-mvc-mongodb-delay200-t500.csv
round1-webflux-mongodb-delay200-t500.csv
round2-mvc-coroutine-mongodb-delay200-t500.csv
round3-mvc-mysql-delay200-t500.csv
round3-webflux-r2dbc-mysql-delay200-t500.csv
```

Grafana 스크린샷은 `docs/screenshots/` 하위에 저장.
