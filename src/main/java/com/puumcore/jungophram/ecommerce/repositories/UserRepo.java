package com.puumcore.jungophram.ecommerce.repositories;

import com.puumcore.jungophram.ecommerce.repositories.entities.Account;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

/**
 * @author Puum Core (Mandela Muriithi)<br>
 * <a href = "https://github.com/puumCore">GitHub: Mandela Muriithi</a><br>
 * Project: ecommerce
 * @version 1.x
 * @since 7/18/2024 5:18 PM
 */
@Repository
public interface UserRepo extends MongoRepository<Account, Long> {
}
