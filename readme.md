# **BareQuery**

**BareQuery** is a lightweight SQL templating framework for dynamic and parameterized query generation with secure execution.

---

## **Features**
- **Dynamic SQL Blocks**: Use `[x | ...]` syntax to conditionally include SQL fragments.
- **Parameterized Queries**: Securely bind `:x` placeholders to avoid SQL injection.
- **Deferred Execution**: Pass a database connection and parameters at execution time.
- **Developer-Friendly Output**: Returns query results as `List<Map<String, Object>>`.

---

## **Installation**

Add the dependency to your `pom.xml`:
```xml
<dependency>
    <groupId>id.levelapp</groupId>
    <artifactId>barequery</artifactId>
    <version>1.0.0</version>
</dependency>
```

---

## **Usage**

### Define and Execute a Query
```java
new BareQuery("SELECT * FROM users WHERE 1=1 [age | AND age > :age] [city | AND city = :city]")
    .execute(conn, Map.of("age", 30, "city", "New York"))
    .forEach(System.out::println);
```

---

## **How It Works**
- **Dynamic Blocks**: `[x | ...]` includes SQL fragments only if `x` is non-null.
- **Rendered SQL**: Automatically generates parameterized queries for secure execution.
- **Output**: Results are returned as a `List<Map<String, Object>>`, where each map represents a row.

---

## **Benefits**
- Dynamic query construction.
- Safe parameter binding to prevent SQL injection.
- Simplified result handling for faster development.

---

## **License**
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.
