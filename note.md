| Feature | Spring MVC (Servlet-based) | Spring WebFlux (Reactive)  |
|---------|------|------|
| Model | Blocking / Imperative | Non-blocking / Declarative |
| Threading | One thread per request. If the request waits for a DB, the thread sits idle. | Event Loop. A few threads handle thousands of concurrent requests. |
| Best For | CRUD apps, traditional REST APIs, and simple logic. | High-concurrency, streaming data, and long-lived connections. |
| Dependencies | Works best with JPA, JDBC, and standard libraries. | Needs reactive drivers (R2DBC, MongoDB Reactive) to stay non-blocking. |

```
export $(grep -v '^#' .env | xargs)
```