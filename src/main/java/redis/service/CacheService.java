package redis.service;

import io.quarkus.logging.Log;
import io.quarkus.redis.datasource.RedisDataSource;
import io.quarkus.redis.datasource.keys.KeyCommands;
import io.quarkus.redis.datasource.value.ValueCommands;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class CacheService {

    private final ValueCommands<String, Object> objectCommands;
    private final KeyCommands<String> keyCommands;

    private final RedisDataSource redisDataSource;
 
    public CacheService(RedisDataSource redisDataSource) {

        this.redisDataSource = redisDataSource;

        this.keyCommands = redisDataSource.key();

        this.objectCommands = redisDataSource.value(String.class, Object.class);
    }

    public void guardarJson(String llave, Object valor) {
        objectCommands.set(llave, valor);
    }

    // Opcional: Método para obtenerlo directamente mapeado a una clase específica
    public <T> T obtenerJson(String llave, Class<T> clase) {
        return redisDataSource.value(clase).get(llave);
    }

    public void eliminar(String redisKey) {
        try {
            keyCommands.del(redisKey);
        } catch (Exception e) {
            Log.error("Error crítico al conectar con Redis para eliminar la llave: " + redisKey, e);
        }
    }
}
