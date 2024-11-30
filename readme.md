# **BareQuery**

**BareQuery** and **BareOperation** are lightweight frameworks designed to simplify SQL interactions. While **BareQuery** focuses on dynamic SQL generation with parameterized execution, **BareOperation** provides easy-to-use CRUD operations without writing SQL manually.

---

## **Features**

### **BareQuery**
- **Dynamic SQL Blocks**: Use `[x | ...]` syntax to conditionally include SQL fragments.
- **Parameterized Queries**: Securely bind `:x` placeholders to avoid SQL injection.
- **Deferred Execution**: Pass a database connection and parameters at execution time.
- **Developer-Friendly Output**: Returns query results as `List<Map<String, Object>>`.

### **BareOperation**
- **CRUD Operations**: Dynamically generates SQL for `INSERT`, `UPDATE`, `DELETE`, and `SELECT` operations.
- **Safe Parameter Binding**: Prevents SQL injection using prepared statements.
- **Flexible Query Construction**: Automatically constructs queries based on provided parameters.
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

### BareQuery Example
```java
new BareQuery("SELECT * FROM users WHERE 1=1 [age | AND age > :age] [city | AND city = :city]")
    .execute(conn, Map.of("age", 30, "city", "New York"))
    .forEach(System.out::println);
```

### BareOperation Examples

#### Insert
```java
new BareOperation("users").insert(conn, Map.of("name", "John Doe", "age", 30));
```

#### Update
```java
new BareOperation("users").update(conn, Map.of("age", 35), Map.of("name", "John Doe"));
```

#### Delete
```java
new BareOperation("users").delete(conn, Map.of("name", "John Doe"));
```

#### Select
```java
new BareOperation("users")
    .select(conn, List.of("id", "name", "age"), Map.of("age", 35))
    .forEach(System.out::println);
```

---

## **How It Works**

### BareQuery
- **Dynamic Blocks**: `[x | ...]` includes SQL fragments only if `x` is non-null.
- **Rendered SQL**: Automatically generates parameterized queries for secure execution.
- **Output**: Results are returned as a `List<Map<String, Object>>`, where each map represents a row.

### BareOperation
- **Dynamic SQL Construction**:
  - Constructs SQL queries based on the provided table, columns, and conditions.
  - Automatically handles query generation for `INSERT`, `UPDATE`, `DELETE`, and `SELECT`.
- **Safe Execution**:
  - All parameters are securely bound using prepared statements to prevent SQL injection.
- **Output**:
  - Results are returned as a `List<Map<String, Object>>` for `SELECT` operations.

---

## **Benefits**
- **BareQuery**:
  - Dynamic query construction.
  - Safe parameter binding.
  - Simplified result handling for faster development.

- **BareOperation**:
  - No need to manually write CRUD queries.
  - Secure and flexible query construction.
  - Easy-to-use API for common database operations.

---

## **License**
This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for details.