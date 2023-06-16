package org.csu.api.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.csu.api.domain.Adress;
import org.springframework.stereotype.Repository;

@Repository
public interface AdressMapper extends BaseMapper<Adress> {
}
