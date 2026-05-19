### Интерактивный режим

```bash
docker exec -it laba9-derby java -jar /derby/lib/derbyrun.jar ij
```

Появится промпт `ij>`. Подключайся:

```sql
CONNECT 'jdbc:derby://localhost:1527/UnivDB;user=db_user;password=db_user';
```

