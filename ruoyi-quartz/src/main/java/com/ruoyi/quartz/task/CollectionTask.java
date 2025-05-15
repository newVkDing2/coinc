package com.ruoyi.quartz.task;

import com.ruoyi.bussiness.domain.TCollectionOrder;
import com.ruoyi.bussiness.mapper.TCollectionOrderMapper;
import com.ruoyi.common.eth.EthUtils;
import com.ruoyi.common.trc.TronUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;

@RequiredArgsConstructor
@Slf4j
@Component("collectionTask")
public class CollectionTask {
    @Resource
    TCollectionOrderMapper tCollectionOrderMapper;
    public void queryCollectionStatus() {
        TCollectionOrder tCollectionOrder = new TCollectionOrder();
       // 1  进行中   2 归集成功  3 归集失败
        tCollectionOrder.setStatus("1");
        List<TCollectionOrder> tCollectionOrders = tCollectionOrderMapper.selectTCollectionOrderList(tCollectionOrder);
        for (TCollectionOrder order: tCollectionOrders) {
            String hash = order.getHash();
            if(order.getChain().equals("ETH")){
                // 0 成功  1失败
                String hashStatus = EthUtils.getHashStatus(hash);
                if(hashStatus.equals("0")){
                    order.setStatus("2");
                    tCollectionOrderMapper.updateTCollectionOrder(order);
                } else if (hashStatus.equals("1")) {
                    order.setStatus("3");
                    tCollectionOrderMapper.updateTCollectionOrder(order);
                }
            }else{
                String hashStatus = TronUtils.getTransactionResult(hash);
                if(hashStatus.equals("0")){
                    order.setStatus("2");
                    tCollectionOrderMapper.updateTCollectionOrder(order);
                } else if (hashStatus.equals("1")) {
                    order.setStatus("3");
                    tCollectionOrderMapper.updateTCollectionOrder(order);
                }
            }

        }
    }
}
