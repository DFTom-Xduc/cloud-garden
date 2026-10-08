package Repository;

import Entity.GameSave;
import java.util.Optional;

public interface GameRepository extends AutoCloseable {
    Optional<GameSave> load();
    void save(GameSave snapshot);
    @Override void close();
}
