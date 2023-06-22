package org.csu.api.persistence;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.csu.api.domain.PayInfo;
import org.springframework.stereotype.Repository;

@Repository
public interface PayInfoMapper extends BaseMapper<PayInfo> {
}
